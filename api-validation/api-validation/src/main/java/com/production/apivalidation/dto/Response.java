package com.production.apivalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Class : Response
Type : DTO
Desc : DTO class to display http response of an API
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response
{
    String url;
    Integer status;
    String response;
}
