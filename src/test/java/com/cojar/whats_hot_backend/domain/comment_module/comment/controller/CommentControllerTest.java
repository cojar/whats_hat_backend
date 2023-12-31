package com.cojar.whats_hot_backend.domain.comment_module.comment.controller;

import com.cojar.whats_hot_backend.domain.comment_module.comment.entity.Comment;
import com.cojar.whats_hot_backend.domain.comment_module.comment.repository.CommentRepository;
import com.cojar.whats_hot_backend.domain.comment_module.comment.service.CommentService;
import com.cojar.whats_hot_backend.domain.member_module.member.entity.Member;
import com.cojar.whats_hot_backend.global.controller.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CommentControllerTest extends BaseControllerTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("POST /api/comments")
    void createComment_OK() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "reviewId": 1,
                                        "content": "댓글내용2",
                                        "tagId": null
                                        }
                                        """
                                )
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("status").value("CREATED"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(3))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.author").value("user1"))
                .andExpect(jsonPath("data.content").value("댓글내용2"))
                .andExpect(jsonPath("data.liked").value(0));
    }

    @Test
    @DisplayName("POST /api/comments")
    void createComment_BadRequest_ReviewNotExist() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        Long review = 3L;

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "reviewId": 3,
                                        "content": "댓글내용2",
                                        "tagId": null
                                        }
                                        """
                                )
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-01-01"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("POST /api/comments")
    void createComment_BadRequest_NotNull() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        Long reviewId = 2L;
        String content = "  ";
        Long tagId = 1L;

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "reviewId": %d,
                                        "content": "%s",
                                        "tagId": %d
                                        }
                                        """.formatted(reviewId, content, tagId).stripIndent())
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-01-02"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("GET /api/comments/1")
    void getComments_OK() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(1))
                .andExpect(jsonPath("data.content").value("댓글내용1"))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.author").value("user1"));
    }

    @Test
    @DisplayName("GET /api/comments/1")
    void getComments_BadRequest_CommentsNotExist() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/comments/3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-02-01"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("GET /api/comments/me")
    void getMyComments_OK() throws Exception {

        // given

        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);


        // when
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/comments/me")
                                .header("Authorization", accessToken)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-03"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data").isArray())
                .andExpect(jsonPath("data[0].content").value("댓글내용1"))
                .andExpect(jsonPath("data[0].id").value("1"))
                .andExpect(jsonPath("data[0].createDate").exists())
                .andExpect(jsonPath("data[0].modifyDate").exists())
                .andExpect(jsonPath("data[0].author").value("user1"));
    }

    @Test
    @DisplayName("GET /api/comments/me")
    void getMyComments_BadRequest_CommentsNotExist() throws Exception {

        // given

        String username = "admin";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);


        // when
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/comments/me")
                                .header("Authorization", accessToken)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-03-01"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("PATCH /api/comments/1")
    void updateComment_OK() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "content": "댓글내용10"
                                        }
                                        """
                                )
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.content").value("댓글내용10"));
    }

    @Test
    @DisplayName("PATCH /api/comments/1")
    void updateComment_BadRequest_NotNull() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        String content = "  ";

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "content": "%s"
                                        }
                                        """.formatted(content).stripIndent())
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("code").value("F-04-04-01"))
                .andExpect(jsonPath("message").value("내용을 작성해주십시오."))
                .andExpect(jsonPath("data[0].field").value("content"))
                .andExpect(jsonPath("data[0].objectName").value("updateComment"))
                .andExpect(jsonPath("data[0].code").value("NotBlank"))
                .andExpect(jsonPath("data[0].defaultMessage").value("must not be blank"))
                .andExpect(jsonPath("data[0].rejectedValue").value("  "));

    }

    @Test
    @DisplayName("PATCH /api/comments/10")
    void updateComment_BadRequest_CommentNotExist() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        String content = "안녕하세요";

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "content": "%s"
                                        }
                                        """.formatted(content).stripIndent())
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-04-02"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("PATCH /api/comments/1")
    void updateComment_BadRequest_MismatchedUser() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        String content = "안녕하세요";

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)
                                .content("""
                                        {
                                        "content": "%s"
                                        }
                                        """.formatted(content).stripIndent())
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-04-03"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("DELETE /api/comments/1")
    void deleteComment_OK() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        delete("/api/comments/1")
                                .header("Authorization", accessToken)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-05"))
                .andExpect(jsonPath("message").exists());

        assertThat(commentRepository.findById(1L)).isEmpty();

    }

    @Test
    @DisplayName("DELETE /api/comments/10")
    void deleteComment_BadRequest_CommentNotExist() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        delete("/api/comments/10")
                                .header("Authorization", accessToken)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-05-01"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("DELETE /api/comments/1")
    void deleteComment_BadRequest_MismatchedUser() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        delete("/api/comments/1")
                                .header("Authorization", accessToken)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-05-02"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("PATCH /api/comments/{id}/like - Like Comment")
    void likeComment_OK() throws Exception {
        // given
        String username = "admin";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1/like")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-06"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.liked").value("1"));
    }

    @Test
    @Transactional
    @DisplayName("PATCH /api/comments/{id}/like - Unlike Comment")
    void unlikeComment_OK() throws Exception {
        // given

        Member member = this.memberService.getUserByUsername("admin");

        Comment comment = this.commentService.getCommentById(1L);

        comment = comment.toBuilder()
                .liked(1L)
                .likedMember(Set.of(member))
                .build();

        this.commentRepository.save(comment);

        String username = "admin";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1/like")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-04-06"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.liked").value("0"));
    }

    @Test
    @DisplayName("PATCH /api/comments/10/like")
    void likeComment_BadRequest_CommentNotExist() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        String content = "안녕하세요";

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/10/like")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", accessToken)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-06-01"))
                .andExpect(jsonPath("message").exists());
    }

    @Test
    @DisplayName("PATCH /api/comments/1/like")
    void likeComment_BadRequest_MatchedUser() throws Exception {

        // given
        String username = "user1";
        String password = "1234";
        String accessToken = this.getAccessToken(username, password);

        // when
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/comments/1/like")
                                .header("Authorization", accessToken)

                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-04-06-02"))
                .andExpect(jsonPath("message").exists());
    }

}
