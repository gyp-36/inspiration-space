package com.is.inspirationspaceclient.work.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 作品附件信息
 */
@Data
@Schema(description = "作品附件信息")
public class WorkAttachmentVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", type = "Long", description = "附件ID")
    private Long id;

    @Schema(name = "file_name", type = "String", description = "文件名")
    private String fileName;

    @Schema(name = "file_size", type = "Long", description = "文件大小")
    private Long fileSize;

    @Schema(name = "file_extension", type = "String", description = "文件扩展名")
    private String fileExtension;

    @Schema(name = "download_url", type = "String", description = "下载链接")
    private String downloadUrl;



}
