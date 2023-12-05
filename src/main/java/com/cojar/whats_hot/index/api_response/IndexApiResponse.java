package com.cojar.whats_hot.index.api_response;

import com.cojar.whats_hot.global.response.ResData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface IndexApiResponse {

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(
            summary = "API 인덱스",
            description = "사용가능한 API 링크들을 반환한다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResData.class),
                    examples = {
                            @ExampleObject(
                                    value = """
                                            {
                                                "status": "OK",
                                                "success": true,
                                                "code": "S-01-01",
                                                "message": "인덱스 링크 목록을 반환합니다",
                                                "_links": {
                                                    "self": {
                                                        "href": "http://localhost:8080/api/index"
                                                    }
                                                }
                                            }
                                            """
                            )
                    })
    )
    public @interface Index {
    }
}