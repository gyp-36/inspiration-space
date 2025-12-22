package com.is.inspirationspaceclient.work.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件上传结果")
public class UploadResultVo {
    @Schema(description = "对象键(存储用)")
    private String objectKey;

    @Schema(description = "预签名URL(预览用)")
    private String url;
}
