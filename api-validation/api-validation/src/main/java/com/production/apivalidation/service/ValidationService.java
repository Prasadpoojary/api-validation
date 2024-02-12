package com.production.apivalidation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.production.apivalidation.controller.ValidationController;
import com.production.apivalidation.dto.Api;
import com.production.apivalidation.dto.Project;
import com.production.apivalidation.dto.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/*
Class : ValidationService
Type : Service
Desc : Service class with different direct/helper methods for code logic and read configurations from 'application.properties' file.
 */

@Service
public class ValidationService
{
    Logger logger= LoggerFactory.getLogger(ValidationController.class);

    //    Read filename of green routes details,request body and projects
    @Value("${configfile.green}")
    private String greenConfigFile;

    //    Read filename of blue routes details,request body and projects
    @Value("${configfile.blue}")
    private String blueConfigFile;

    //    Read file path of blue and green config file
    @Value("${configfile.path}")
    private String configPath;

    //    Read retry count of config file read
    @Value("${configfile.retry}")
    private int fileReadRetry;

    //    Read retry count of api call
    @Value("${api.retry}")
    private int apiCallRetry;


    //    Temporary variable for retry count
    private Integer tempFileReadRetry;
    private Integer tempApiCallRetry;



    /*
    Read JSON config file based on type blue/green.
    Retry : Until 'fileReadRetry' count if any I/O error
    Returns : List of 'Project' objects.
    Scope : Private
     */
    private List<Project> readFile(String type) {
        logger.info("Reading {} config file",type);
        String filename=type.equals("green") ? this.greenConfigFile : this.blueConfigFile;
        String path=configPath+filename;
        List<Project> projects=new ArrayList<>();
        ObjectMapper mapper=new ObjectMapper();
        try
        {
            projects=mapper.readValue(new File(path), new TypeReference<List<Project>>(){});
        }
        catch (IOException ioException)
        {
            if(this.tempFileReadRetry==null)
            {
                this.tempFileReadRetry=this.fileReadRetry;
            }

            if(this.tempFileReadRetry > 0)
            {
                tempFileReadRetry--;
                logger.error("I/O Exception while reading config file : {}",ioException.getMessage());
                logger.warn("Retrying file read, Retry limit : {}",this.tempFileReadRetry);

                return readFile(type);
            }
            else
            {
                logger.error("Retry limit exceeded, returned empty List of 'Projects' ");
                return new ArrayList<>();
            }
        }
        finally
        {
            this.tempFileReadRetry=fileReadRetry;
        }

        logger.info("file read and returned list of 'Projects'");
        return projects;
    }


    /*
    Read all Project name.
    Returns : List of 'String' .
    Scope : Public
     */
    public List<String> getAllProjectName()
    {
        logger.info("Reading Project names from 'blue' config file");
        return this.readFile("blue").stream().map(Project::getProject).collect(Collectors.toList());
    }


    /*
    Format HTTP response body JSON string, add newline '<br>' on every comma and braces.
    Returns : Formatted JSON string.
    Scope : Private
     */
    private String parseResponse(String response)
    {
        logger.info("Parsing JSON response");
        response=response.replace("{","{ <br> ")
                .replace(",",", <br> ")
                .replace("}"," <br> }");

        return response;
    }


    /*
    Fetch API url using RestTemplate and handle exception.
    Retry : Until 'apiCallRetry' count if any Exception except HttpServerErrorException
    Returns : 'Response' objects.
    Scope : Private
     */
    private Response fetchApi(Api api)
    {
        logger.info("fetching API : {} , Method : {}",api.getUrl(),api.getMethod());
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = null;
        Response apiResponse=new Response();
        headers.set("Content-Type", "application/json");


        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        if(api.getMethod().equals("POST"))
        {
            requestEntity=new HttpEntity<>(api.getBody(),headers);
        }
        
        RestTemplate restTemplate=new RestTemplate();

        try
        {
            apiResponse.setUrl(api.getUrl());
            response = restTemplate.exchange(
                    api.getUrl(), api.getMethod().equals("GET") ? HttpMethod.GET : HttpMethod.POST, requestEntity, String.class);
            apiResponse.setStatus(response.getStatusCode().value());
            apiResponse.setResponse(response.getBody());

            if(apiResponse.getStatus()!=0)
            {
                apiResponse.setResponse(parseResponse(apiResponse.getResponse()));
            }
        }
        catch(HttpServerErrorException | HttpClientErrorException error)
        {
            logger.error("Exception while fetching API : {}",error.getMessage());

            if(error.getClass().equals(HttpClientErrorException.class))
            {
                if(this.tempApiCallRetry==null)
                {
                    this.tempApiCallRetry=this.apiCallRetry;
                }

                if(this.tempApiCallRetry > 0)
                {
                    tempApiCallRetry--;
                    logger.warn("HttpClientErrorException occurred, retrying API fetch. Retry limit : {}",this.tempApiCallRetry);
                    return fetchApi(api);
                }
                else
                {
                    logger.error("Retry limit exceeded, returned 'HttpClientErrorException' Response ");

                }
            }

            String body=error.getResponseBodyAsString();
            int status=error.getStatusCode().value();
            apiResponse.setStatus(status);
            apiResponse.setResponse(body);

            if(apiResponse.getStatus()!=0)
            {
                apiResponse.setResponse(parseResponse(apiResponse.getResponse()));
            }
        }
        catch (Exception exception)
        {
            logger.error("Exception while fetching API : {}",exception.getMessage());
            if(this.tempApiCallRetry==null)
            {
                this.tempApiCallRetry=this.apiCallRetry;
            }

            if(this.tempApiCallRetry > 0)
            {
                tempApiCallRetry--;
                logger.warn("Exception occurred, retrying API fetch. Retry limit : {}",this.tempApiCallRetry);
                return fetchApi(api);
            }
            else
            {
                logger.error("Retry limit exceeded, returned 'Exception' Response ");
            }

            String body=response!=null? response.getBody() : exception.getMessage();
            int status=response!=null?response.getStatusCode().value():0;
            apiResponse.setStatus(status);
            apiResponse.setResponse(body);

            if(apiResponse.getStatus()!=0)
            {
                apiResponse.setResponse(parseResponse(apiResponse.getResponse()));
            }
        }
        finally
        {
            this.tempApiCallRetry=this.apiCallRetry;
        }

        return apiResponse;
    }


    /*
    validate selected project from specific route
    Returns : List of 'Response' objects.
    Scope : Public
     */
    public List<Response> validate(String type, String selectedProject)
    {
        List<Response> responses=new ArrayList<>();
        List<Api> apis=new ArrayList<>();
        try
        {
            logger.info("Reading list of API of {} project",selectedProject);
            apis=readFile(type).stream().filter(project -> project.getProject().equals(selectedProject)).toList().get(0).getApi();
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            logger.error("No data found for {} project",selectedProject);
        }

        for(Api api : apis)
        {
            responses.add(fetchApi(api));
        }

        return responses;
    }
}
