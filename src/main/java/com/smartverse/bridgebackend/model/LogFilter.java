package com.smartverse.bridgebackend.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class LogFilter {

    private String id;
    private String path;
    private String method;
    private int status;
    private long duration;
    private LocalDateTime timestamp;
}
