package com.cojar.whats_hot_backend.domain.spot_module.spot.controller;

import com.cojar.whats_hot_backend.domain.base_module.file.service.FileService;
import com.cojar.whats_hot_backend.domain.spot_module.category.entity.Category;
import com.cojar.whats_hot_backend.domain.spot_module.category.service.CategoryService;
import com.cojar.whats_hot_backend.domain.spot_module.menu_item.dto.MenuItemDto;
import com.cojar.whats_hot_backend.domain.spot_module.menu_item.entity.MenuItem;
import com.cojar.whats_hot_backend.domain.spot_module.menu_item.service.MenuItemService;
import com.cojar.whats_hot_backend.domain.spot_module.spot.entity.Spot;
import com.cojar.whats_hot_backend.domain.spot_module.spot.request.SpotRequest;
import com.cojar.whats_hot_backend.domain.spot_module.spot.service.SpotService;
import com.cojar.whats_hot_backend.domain.spot_module.spot_hashtag.entity.SpotHashtag;
import com.cojar.whats_hot_backend.domain.spot_module.spot_hashtag.service.SpotHashtagService;
import com.cojar.whats_hot_backend.domain.spot_module.spot_image.entity.SpotImage;
import com.cojar.whats_hot_backend.domain.spot_module.spot_image.service.SpotImageService;
import com.cojar.whats_hot_backend.global.controller.BaseControllerTest;
import com.cojar.whats_hot_backend.global.response.ResCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.config.http.MatcherType.mvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


class SpotControllerTest extends BaseControllerTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpotService spotService;

    @Autowired
    private SpotHashtagService spotHashtagService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private SpotImageService spotImageService;

    @Autowired
    private FileService fileService;

    @Transactional
    @Test
    @DisplayName("post:/api/spots - created, S-02-01")
    public void createSpot_Created() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Category category = this.categoryService.getCategoryById(3L);
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag1 = "뷔페", hashtag2 = "초밥";
        String menuName1 = "평일점심", menuPrice1 = "20,900원";
        String menuName2 = "평일저녁", menuPrice2 = "24,900원";
        String menuName3 = "주말/공휴일", menuPrice3 = "26,900원";

        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(category.getId())
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag1, hashtag2))
                .menuItems(List.of(
                        MenuItemDto.of(menuName1, menuPrice1),
                        MenuItemDto.of(menuName2, menuPrice2),
                        MenuItemDto.of(menuName3, menuPrice3)
                ))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("status").value("CREATED"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-02-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").exists())
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.category").value(category.toLine()))
                .andExpect(jsonPath("data.name").value(name))
                .andExpect(jsonPath("data.address").value(address))
                .andExpect(jsonPath("data.contact").value(contact))
                .andExpect(jsonPath("data.averageScore").value(0.0))
                .andExpect(jsonPath("data.hashtags[0]").value(hashtag1))
                .andExpect(jsonPath("data.hashtags[1]").value(hashtag2))
                .andExpect(jsonPath("data.menuItems[0].name").value(menuName1))
                .andExpect(jsonPath("data.menuItems[0].price").value(menuPrice1))
                .andExpect(jsonPath("data.menuItems[1].name").value(menuName2))
                .andExpect(jsonPath("data.menuItems[1].price").value(menuPrice2))
                .andExpect(jsonPath("data.menuItems[2].name").value(menuName3))
                .andExpect(jsonPath("data.menuItems[2].price").value(menuPrice3))
                .andExpect(jsonPath("data.imageUri[0]").exists())
                .andExpect(jsonPath("data.imageUri[1]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @ParameterizedTest
    @MethodSource("argsFor_createSpot_BadRequest_NotBlank")
    @DisplayName("post:/api/spots - bad request not blank, F-02-01-01")
    public void createSpot_BadRequest_NotBlank(Long categoryId,
                                               String name,
                                               String address,
                                               String contact,
                                               String hashtag,
                                               String menuName,
                                               String menuPrice) throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-01-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
        ;

        if (categoryId == null) resultActions.andExpect(jsonPath("data[0].rejectedValue").doesNotExist());
        else resultActions.andExpect(jsonPath("data[0].rejectedValue").value(""));

        checkNotCreated(checkList);
    }

    private static Stream<Arguments> argsFor_createSpot_BadRequest_NotBlank() {
        return Stream.of(
                Arguments.of(null, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(3L, "", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(3L, "쿠우쿠우 대전둔산점", "", "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(3L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "", "뷔페", "평일점심", "20,900원"),
                Arguments.of(3L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "", "평일점심", "20,900원"),
                Arguments.of(3L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "", "20,900원"),
                Arguments.of(3L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "평일점심", "")
        );
    }

    @Test
    @DisplayName("post:/api/spots - bad request category not exist, F-02-01-02")
    public void createSpot_BadRequest_CategoryNotExist() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        Long categoryId = 1000000000L;
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-01-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(categoryId))
        ;

        checkNotCreated(checkList);
    }

    @ParameterizedTest
    @MethodSource("argsFor_createSpot_BadRequest_InvalidCategoryId")
    @DisplayName("post:/api/spots - bad request invalid categoryId, F-02-01-03")
    public void createSpot_BadRequest_InvalidCategoryId(Long categoryId) throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-01-03"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(categoryId))
        ;

        checkNotCreated(checkList);
    }

    private static Stream<Arguments> argsFor_createSpot_BadRequest_InvalidCategoryId() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(2L)
        );
    }

    @Test
    @DisplayName("post:/api/spots - bad request duplicated name and address, F-02-01-04")
    public void createSpot_BadRequest_DuplicatedNameAndAddress() throws Exception {

        Spot spot = this.spotService.getSpotById(1L);

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        Long categoryId = 3L;
        String name = "장소1";
        String address = "대전 서구 대덕대로 179";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-01-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue[0]").value(name))
                .andExpect(jsonPath("data[0].rejectedValue[1]").value(address))
        ;

        checkNotCreated(checkList);
    }

    @Test
    @DisplayName("post:/api/spots - bad request content type, F-00-00-01")
    public void createSpot_BadRequest_ContentType() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        Long categoryId = 3L;
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.TEXT_MARKDOWN_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-00-00-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.TEXT_MARKDOWN_VALUE))
        ;

        checkNotCreated(checkList);
    }

    @Test
    @DisplayName("post:/api/spots - bad request content type, F-00-00-02")
    public void createSpot_BadRequest_Extension() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        Long categoryId = 3L;
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "gif";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_GIF_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-00-00-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.IMAGE_GIF_VALUE))
        ;

        checkNotCreated(checkList);
    }

    @Test
    @DisplayName("post:/api/spots - bad request multiple file error, F-00-00-01 & F-00-00-02")
    public void createSpot_BadRequest_MultipleFileError() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        List<Long> checkList = getCheckListNotCreated();

        Long categoryId = 3L;
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag = "뷔페";
        String menuName = "평일점심", menuPrice = "20,900원";
        SpotRequest.CreateSpot request = SpotRequest.CreateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName1 = "test";
        String ext1 = "a";
        Resource resource1 = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName1, ext1));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName1, ext1),
                MediaType.TEXT_MARKDOWN_VALUE,
                resource1.getInputStream()
        );
        String fileName2 = "test";
        String ext2 = "gif";
        Resource resource2 = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName2, ext2));
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName2, ext2),
                MediaType.IMAGE_GIF_VALUE,
                resource2.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.POST, "/api/spots")
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code[0]").value("F-00-00-01"))
                .andExpect(jsonPath("code[1]").value("F-00-00-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.TEXT_MARKDOWN_VALUE))
                .andExpect(jsonPath("data[1].field").exists())
                .andExpect(jsonPath("data[1].objectName").exists())
                .andExpect(jsonPath("data[1].code").exists())
                .andExpect(jsonPath("data[1].defaultMessage").exists())
                .andExpect(jsonPath("data[1].rejectedValue").value(MediaType.IMAGE_GIF_VALUE))
        ;

        checkNotCreated(checkList);
    }

    private List<Long> getCheckListNotCreated() {
        return List.of(
                this.spotService.count() + 1,
                this.spotHashtagService.count(),
                this.menuItemService.count(),
                this.spotImageService.count(),
                this.fileService.count()
        );
    }

    private void checkNotCreated(List<Long> checkList) {
        int i = 0;
        assertThat(this.spotService.getSpotById(checkList.get(i++))).isNull();
        assertThat(this.spotHashtagService.count()).isEqualTo(checkList.get(i++));
        assertThat(this.menuItemService.count()).isEqualTo(checkList.get(i++));
        assertThat(this.spotImageService.count()).isEqualTo(checkList.get(i++));
        assertThat(this.fileService.count()).isEqualTo(checkList.get(i));
    }

    @Transactional
    @Test
    @DisplayName("patch:/api/spots/{id} - ok, S-02-04")
    public void updateSpot_OK() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Category category = this.categoryService.getCategoryById(4L);
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag1 = "뷔페", hashtag2 = "초밥";
        List<String> hashtags = List.of(hashtag1, hashtag2);
        String menuName1 = "평일점심", menuPrice1 = "20,900원";
        String menuName2 = "평일저녁", menuPrice2 = "24,900원";
        String menuName3 = "주말/공휴일", menuPrice3 = "26,900원";
        List<MenuItemDto> menuItemDtos = List.of(
                MenuItemDto.of(menuName1, menuPrice1),
                MenuItemDto.of(menuName2, menuPrice2),
                MenuItemDto.of(menuName3, menuPrice3)
        );
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .categoryId(category.getId())
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(hashtags)
                .menuItems(menuItemDtos)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-02-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(id))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.category").value(category.toLine()))
                .andExpect(jsonPath("data.name").value(name))
                .andExpect(jsonPath("data.address").value(address))
                .andExpect(jsonPath("data.contact").value(contact))
                .andExpect(jsonPath("data.hashtags[0]").value(hashtag1))
                .andExpect(jsonPath("data.hashtags[1]").value(hashtag2))
                .andExpect(jsonPath("data.menuItems[0].name").value(menuName1))
                .andExpect(jsonPath("data.menuItems[0].price").value(menuPrice1))
                .andExpect(jsonPath("data.menuItems[1].name").value(menuName2))
                .andExpect(jsonPath("data.menuItems[1].price").value(menuPrice2))
                .andExpect(jsonPath("data.menuItems[2].name").value(menuName3))
                .andExpect(jsonPath("data.menuItems[2].price").value(menuPrice3))
                .andExpect(jsonPath("data.imageUri[0]").exists())
                .andExpect(jsonPath("data.imageUri[1]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        Spot spot = this.spotService.getSpotById(id);
        List<SpotHashtag> spotHashtags = this.spotHashtagService.getAllBySpot(spot);
        assertThat(spotHashtags.size()).isEqualTo(hashtags.size());
        for (int i = 0; i < spotHashtags.size(); i++) {
            assertThat(spotHashtags.get(i).getHashtag().getName()).isEqualTo(hashtags.get(i));
        }
        List<MenuItem> menuItems = this.menuItemService.getAllBySpot(spot);
        assertThat(menuItems.size()).isEqualTo(menuItemDtos.size());
        for (int i = 0; i < menuItems.size(); i++) {
            assertThat(menuItems.get(i).getName()).isEqualTo(menuItemDtos.get(i).getName());
            assertThat(menuItems.get(i).getPrice()).isEqualTo(menuItemDtos.get(i).getPrice());
        }
        List<SpotImage> spotImages = this.spotImageService.getAllBySpot(spot);
        assertThat(spotImages.size()).isEqualTo(2);
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("argsFor_updateSpot_OK_PartialInput_Request")
    @DisplayName("patch:/api/spots/{id} - ok partial input request, S-02-04")
    public void updateSpot_OK_PartialInput_Request(Long categoryId,
                                                   String name,
                                                   String address,
                                                   String contact,
                                                   String hashtag,
                                                   String menuName,
                                                   String menuPrice) throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot spot = this.spotService.getSpotById(id);
        List<String> hashtags = hashtag != null ? List.of(hashtag) : null;
        List<MenuItemDto> menuItemDtos = menuName != null & menuPrice != null ? List.of(MenuItemDto.of(menuName, menuPrice)) : null;
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .categoryId(categoryId)
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(hashtags)
                .menuItems(menuItemDtos)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-02-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(id))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.category").value(categoryId != null ? this.categoryService.getCategoryById(categoryId).toLine() : spot.getCategory().toLine()))
                .andExpect(jsonPath("data.name").value(name != null ? name : spot.getName()))
                .andExpect(jsonPath("data.address").value(address != null ? address : spot.getAddress()))
                .andExpect(jsonPath("data.contact").value(contact != null ? contact : spot.getContact()))
                .andExpect(jsonPath("data.hashtags[0]").value(hashtag != null ? hashtag : spot.getHashtags().get(0).getHashtag().getName()))
                .andExpect(jsonPath("data.menuItems[0].name").value(menuName != null ? menuName : spot.getMenuItems().get(0).getName()))
                .andExpect(jsonPath("data.menuItems[0].price").value(menuPrice != null ? menuPrice : spot.getMenuItems().get(0).getPrice()))
                .andExpect(jsonPath("data.imageUri[0]").exists())
                .andExpect(jsonPath("data.imageUri[1]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    private static Stream<Arguments> argsFor_updateSpot_OK_PartialInput_Request() {
        return Stream.of(
                Arguments.of(null, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(4L, null, "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(4L, "쿠우쿠우 대전둔산점", null, "042-489-6274", "뷔페", "평일점심", "20,900원"),
                Arguments.of(4L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", null, "뷔페", "평일점심", "20,900원"),
                Arguments.of(4L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", null, "평일점심", "20,900원"),
                Arguments.of(4L, "쿠우쿠우 대전둔산점", "대전 서구 대덕대로233번길 17 해운빌딩 4층", "042-489-6274", "뷔페", null, null)
        );
    }

    @Transactional
    @Test
    @DisplayName("patch:/api/spots/{id} - ok partial input images, S-02-04")
    public void updateSpot_OK_PartialInput_Images() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Category category = this.categoryService.getCategoryById(4L);
        String name = "쿠우쿠우 대전둔산점";
        String address = "대전 서구 대덕대로233번길 17 해운빌딩 4층";
        String contact = "042-489-6274";
        String hashtag1 = "뷔페", hashtag2 = "초밥";
        List<String> hashtags = List.of(hashtag1, hashtag2);
        String menuName1 = "평일점심", menuPrice1 = "20,900원";
        String menuName2 = "평일저녁", menuPrice2 = "24,900원";
        String menuName3 = "주말/공휴일", menuPrice3 = "26,900원";
        List<MenuItemDto> menuItemDtos = List.of(
                MenuItemDto.of(menuName1, menuPrice1),
                MenuItemDto.of(menuName2, menuPrice2),
                MenuItemDto.of(menuName3, menuPrice3)
        );
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .categoryId(category.getId())
                .name(name)
                .address(address)
                .contact(contact)
                .hashtags(hashtags)
                .menuItems(menuItemDtos)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-02-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(id))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.category").value(category.toLine()))
                .andExpect(jsonPath("data.name").value(name))
                .andExpect(jsonPath("data.address").value(address))
                .andExpect(jsonPath("data.contact").value(contact))
                .andExpect(jsonPath("data.hashtags[0]").value(hashtag1))
                .andExpect(jsonPath("data.hashtags[1]").value(hashtag2))
                .andExpect(jsonPath("data.menuItems[0].name").value(menuName1))
                .andExpect(jsonPath("data.menuItems[0].price").value(menuPrice1))
                .andExpect(jsonPath("data.menuItems[1].name").value(menuName2))
                .andExpect(jsonPath("data.menuItems[1].price").value(menuPrice2))
                .andExpect(jsonPath("data.menuItems[2].name").value(menuName3))
                .andExpect(jsonPath("data.menuItems[2].price").value(menuPrice3))
                .andExpect(jsonPath("data.imageUri[0]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Transactional
    @Test
    @DisplayName("patch:/api/spots/{id} - ok partial input only images, S-02-04")
    public void updateSpot_OK_PartialInput_OnlyImages() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_PNG_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("OK"))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value("S-02-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data.id").value(id))
                .andExpect(jsonPath("data.createDate").exists())
                .andExpect(jsonPath("data.modifyDate").exists())
                .andExpect(jsonPath("data.imageUri[0]").exists())
                .andExpect(jsonPath("data.imageUri[1]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request not exist, F-02-04-01")
    public void updateSpot_BadRequest_NotExist() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 100000000L;
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-04-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue[0]").value(id))
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @ParameterizedTest
    @MethodSource("argsFor_updateSpot_BadRequest_NotBlank")
    @DisplayName("patch:/api/spots/{id} - bad request not blank, F-02-04-02")
    public void updateSpot_BadRequest_NotBlank(String hashtag, String menuName, String menuPrice) throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .hashtags(List.of(hashtag))
                .menuItems(List.of(MenuItemDto.of(menuName, menuPrice)))
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-04-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(""))
                .andExpect(jsonPath("_links.index").exists())
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    private static Stream<Arguments> argsFor_updateSpot_BadRequest_NotBlank() {
        return Stream.of(
                Arguments.of("", "평일점심", "20,900원"),
                Arguments.of("뷔페", "", "20,900원"),
                Arguments.of("뷔페", "평일점심", "")
        );
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request category not exist, F-02-04-03")
    public void updateSpot_BadRequest_CategoryNotExist() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Long categoryId = 10000000L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .categoryId(categoryId)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-04-03"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(categoryId))
                .andExpect(jsonPath("_links.index").exists())
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    @ParameterizedTest
    @MethodSource("argsFor_updateSpot_BadRequest_InvalidCategoryId")
    @DisplayName("patch:/api/spots/{id} - bad request category not exist, F-02-04-04")
    public void updateSpot_BadRequest_InvalidCategoryId(Long categoryId) throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .categoryId(categoryId)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-04-04"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(categoryId))
                .andExpect(jsonPath("_links.index").exists())
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    private static Stream<Arguments> argsFor_updateSpot_BadRequest_InvalidCategoryId() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(2L)
        );
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request duplicated name and address, F-02-04-05")
    public void updateSpot_BadRequest_DuplicatedNameAndAddress() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        String name = "장소2";
        String address = "대전 서구 대덕대로 179";
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .name(name)
                .address(address)
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value("F-02-04-05"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue[0]").value(name))
                .andExpect(jsonPath("data[0].rejectedValue[1]").value(address))
                .andExpect(jsonPath("_links.index").exists())
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request content type, F-00-00-01")
    public void updateSpot_BadRequest_ContentType() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "png";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.TEXT_MARKDOWN_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code[0]").value("F-00-00-01"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.TEXT_MARKDOWN_VALUE))
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request extension, F-00-00-02")
    public void updateSpot_BadRequest_Extension() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName = "test";
        String ext = "gif";
        Resource resource = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName, ext));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName, ext),
                MediaType.IMAGE_GIF_VALUE,
                resource.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code[0]").value("F-00-00-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.IMAGE_GIF_VALUE))
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    @Test
    @DisplayName("patch:/api/spots/{id} - bad request multiple file error, F-00-00-01 & F-00-00-02")
    public void updateSpot_BadRequest_MultipleFileError() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;
        Spot before = this.spotService.getSpotById(id);
        SpotRequest.UpdateSpot request = SpotRequest.UpdateSpot.builder()
                .build();
        MockMultipartFile _request = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                this.objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );

        String fileName1 = "test";
        String ext1 = "a";
        Resource resource1 = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName1, ext1));
        MockMultipartFile _file1 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName1, ext1),
                MediaType.TEXT_MARKDOWN_VALUE,
                resource1.getInputStream()
        );

        String fileName2 = "test";
        String ext2 = "gif";
        Resource resource2 = resourceLoader.getResource("classpath:/static/image/%s.%s".formatted(fileName2, ext2));
        MockMultipartFile _file2 = new MockMultipartFile(
                "images",
                "%s.%s".formatted(fileName2, ext2),
                MediaType.IMAGE_GIF_VALUE,
                resource2.getInputStream()
        );

        // when
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.PATCH, "/api/spots/%s".formatted(id))
                        .file(_request)
                        .file(_file1)
                        .file(_file2)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value("BAD_REQUEST"))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code[0]").value("F-00-00-01"))
                .andExpect(jsonPath("code[1]").value("F-00-00-02"))
                .andExpect(jsonPath("message").exists())
                .andExpect(jsonPath("data[0].field").exists())
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue").value(MediaType.TEXT_MARKDOWN_VALUE))
                .andExpect(jsonPath("data[1].field").exists())
                .andExpect(jsonPath("data[1].objectName").exists())
                .andExpect(jsonPath("data[1].code").exists())
                .andExpect(jsonPath("data[1].defaultMessage").exists())
                .andExpect(jsonPath("data[1].rejectedValue").value(MediaType.IMAGE_GIF_VALUE))
        ;

        Spot after = this.spotService.getSpotById(id);
        checkNotUpdated(before, after);
    }

    private void checkNotUpdated(Spot before, Spot after) {
        assertThat(before.getCategory().toLine()).isEqualTo(after.getCategory().toLine());
        assertThat(before.getName()).isEqualTo(after.getName());
        assertThat(before.getAddress()).isEqualTo(after.getAddress());
        assertThat(before.getLatitude()).isEqualTo(after.getLatitude());
        assertThat(before.getLongitude()).isEqualTo(after.getLongitude());
        assertThat(before.getContact()).isEqualTo(after.getContact());
        List<SpotHashtag> beforeHashtags = this.spotHashtagService.getAllBySpot(before);
        List<SpotHashtag> afterHashtags = this.spotHashtagService.getAllBySpot(after);
        assertThat(beforeHashtags.size()).isEqualTo(afterHashtags.size());
        for (int i = 0; i < beforeHashtags.size(); i++) {
            assertThat(beforeHashtags.get(i).getHashtag().getName()).isEqualTo(afterHashtags.get(i).getHashtag().getName());
        }
        List<MenuItem> beforeItems = this.menuItemService.getAllBySpot(before);
        List<MenuItem> afterItems = this.menuItemService.getAllBySpot(after);
        assertThat(beforeItems.size()).isEqualTo(afterItems.size());
        for (int i = 0; i < beforeItems.size(); i++) {
            assertThat(beforeItems.get(i).getName()).isEqualTo(afterItems.get(i).getName());
            assertThat(beforeItems.get(i).getPrice()).isEqualTo(afterItems.get(i).getPrice());
        }
        List<SpotImage> beforeImages = this.spotImageService.getAllBySpot(before);
        List<SpotImage> afterImages = this.spotImageService.getAllBySpot(after);
        assertThat(beforeImages.size()).isEqualTo(afterImages.size());
        for (int i = 0; i < beforeImages.size(); i++) {
            assertThat(beforeImages.get(i).getImage().getUuid()).isEqualTo(afterImages.get(i).getImage().getUuid());
        }
    }

    @Transactional
    @Test
    @DisplayName("delete:/api/spots - ok, S-02-05")
    public void deleteSpot_OK() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 1L;

        // when
        ResultActions resultActions = this.mockMvc
                .perform(delete("/api/spots/%s".formatted(id))
                        .header("Authorization", accessToken)
                        .contentType(MediaType.ALL)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(ResCode.S_02_05.getStatus().name()))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value(ResCode.S_02_05.getCode()))
                .andExpect(jsonPath("message").value(ResCode.S_02_05.getMessage()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;

        assertThat(this.spotService.getSpotById(id) == null).isTrue();
    }

    @Test
    @DisplayName("delete:/api/spots - bad request not exist, F-02-05-01")
    public void deleteSpot_BadRequest_NotExist() throws Exception {

        // given
        String username = "admin";
        String password = "1234";
        String accessToken = "Bearer " + this.memberService.getAccessToken(loginReq.of(username, password));

        Long id = 100000000L;

        // when
        ResultActions resultActions = this.mockMvc
                .perform(delete("/api/spots/%s".formatted(id))
                        .header("Authorization", accessToken)
                        .contentType(MediaType.ALL)
                        .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(ResCode.F_02_05_01.getStatus().name()))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value(ResCode.F_02_05_01.getCode()))
                .andExpect(jsonPath("message").value(ResCode.F_02_05_01.getMessage()))
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue[0]").value(id.toString()))
                .andExpect(jsonPath("_links.index").exists())
        ;
    }


    @Test
    @DisplayName("GET:api/Spots/{id} - detail, S-02-03")
    void getSpot_ok() throws Exception {

        //given
        Long id = 1L;


        // When
        ResultActions resultActions = this.mockMvc
                .perform(multipart(HttpMethod.GET, "/api/spots/%s".formatted(id))
                                .contentType(MediaType.ALL_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                )
                .andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(ResCode.S_02_03.getStatus().name()))
                .andExpect(jsonPath("success").value("true"))
                .andExpect(jsonPath("code").value(ResCode.S_02_03.getCode()))
                .andExpect(jsonPath("message").value(ResCode.S_02_03.getMessage()))
                .andExpect(jsonPath("data.id").value(1))
                .andExpect(jsonPath("data.name").exists())
                .andExpect(jsonPath("data.category").exists())
                .andExpect(jsonPath("data.address").exists())
                .andExpect(jsonPath("data.contact").exists())
                .andExpect(jsonPath("data.averageScore").exists())
                .andExpect(jsonPath("data.hashtags").exists())
                .andExpect(jsonPath("data.reviews").exists());

    }

    @Test
    @DisplayName("GET:api/spots/{id} - bad request not exist, F-02-04-01")
    void getSpot_BadRequest_Spot_NotExist() throws Exception {

        // given
        Long id = 9L;

        // when
        ResultActions resultActions = mockMvc
                .perform(multipart(HttpMethod.GET, "/api/spots/%s".formatted(id))
                                .contentType(MediaType.ALL_VALUE)
                                .accept(MediaTypes.HAL_JSON)

                )
                .andDo(print());


        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(ResCode.F_02_04_01.getStatus().name()))
                .andExpect(jsonPath("success").value("false"))
                .andExpect(jsonPath("code").value(ResCode.F_02_04_01.getCode()))
                .andExpect(jsonPath("message").value(ResCode.F_02_04_01.getMessage()))
                .andExpect(jsonPath("data[0].objectName").exists())
                .andExpect(jsonPath("data[0].code").exists())
                .andExpect(jsonPath("data[0].defaultMessage").exists())
                .andExpect(jsonPath("data[0].rejectedValue[0]").value(id.toString()))
                .andExpect(jsonPath("_links.index").exists());

    }


}