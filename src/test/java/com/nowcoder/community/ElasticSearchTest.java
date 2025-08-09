package com.nowcoder.community;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete() {
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {

        HighlightFieldParameters titleFieldParams = HighlightFieldParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withNumberOfFragments(0)
                .build();
// 创建内容高亮字段参数
        HighlightFieldParameters contentFieldParams = HighlightFieldParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withNumberOfFragments(0)
                .build();
        // 创建高亮字段
        HighlightField titleHighlightField = new HighlightField("title", titleFieldParams);
        HighlightField contentHighlightField = new HighlightField("content", contentFieldParams);
        // 创建高亮对象，包含多个高亮字段
        List<HighlightField> highlightFieldList = new ArrayList<>();
        highlightFieldList.add(titleHighlightField);
        highlightFieldList.add(contentHighlightField);
        Highlight highlight = new Highlight(highlightFieldList);
        // 创建HighlightQuery
        HighlightQuery highlightQuery = new HighlightQuery(highlight, null);

        Query elasticsearchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s
                                        .match(m -> m
                                                .field("title")
                                                .query("互联网寒冬")
                                        )
                                )
                                .should(s -> s
                                        .match(m -> m
                                                .field("content")
                                                .query("互联网寒冬")
                                        )
                                )
                        )
                )
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("type")
                                .order(SortOrder.Desc)
                        )
                ))
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("score")
                                .order(SortOrder.Desc)
                        )
                ))
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("createTime")
                                .order(SortOrder.Desc)
                        )
                ))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightQuery(highlightQuery)
                .build();
        // todo 以上没有进行高亮处理
        SearchHits<DiscussPost> searchHits = elasticsearchTemplate.search(
                elasticsearchQuery,
                DiscussPost.class
        );

        // 1. 获取总命中数（匹配到的所有记录数）
        long totalHits = searchHits.getTotalHits();
        System.out.println("总匹配记录数: " + totalHits);

// 2. 获取当前页的记录数（实际返回的记录数）
        int currentPageHits = searchHits.getSearchHits().size();
        System.out.println("当前页记录数: " + currentPageHits);

// 3. 获取分页信息
        PageRequest pageRequest = (PageRequest) elasticsearchQuery.getPageable();
        int pageNumber = pageRequest.getPageNumber();   // 当前页码（从0开始）
        int pageSize = pageRequest.getPageSize();       // 每页大小
        System.out.println("当前页码: " + (pageNumber + 1)); // 转换为人类可读的页码（从1开始）
        System.out.println("每页大小: " + pageSize);

// 4. 计算总页数
        int totalPages = (int) Math.ceil((double) totalHits / pageSize);
        System.out.println("总页数: " + totalPages);

        searchHits.getSearchHits().forEach(hit -> {
            System.out.println("Found by ElasticsearchTemplate: " + hit.getContent());
            // 处理高亮（如果有配置）
            Map<String, List<String>> hightMap =  hit.getHighlightFields();
            System.out.println(hightMap.size());
            if (!hit.getHighlightFields().isEmpty()) {
                hit.getHighlightFields().forEach((field, fragments) -> {
                    System.out.println("Highlight in " + field + ": " + Arrays.toString(new List[]{fragments}));
                });
            }
        });

    }


}
