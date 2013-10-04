package com.hlabs.hbrowse;

/**
 * Created with IntelliJ IDEA.
 * User: naveen
 * Date: 10/2/13
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */

import freemarker.template.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import static spark.Spark.*;


public class App {

    public static void main(String[] args) throws IOException {
        setPort(8080);
        final Configuration cfg = configureFreemarker();

        get(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {

                //freemarker needs a Writer to render the final Html code
                StringWriter sw = new StringWriter();

                //params used in the template files
                //passed the sublayout filename and the title page
                HashMap params = getPageParams("home.ftl", "Home page");
                try {

                    //template engine processing
                    cfg.getTemplate("main.ftl").process(params, sw);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //return the rendered html code
                return sw.toString();
            }
        });

        post(new Route("/get_result") {
            @Override
            public Object handle(Request request, Response response) {
                String userQuery = request.queryParams("qu");

                return "Hello World: " +  userQuery;
            }
        });


        post(new Route("/createTable") {
            @Override
            public Object handle(Request request, Response response) {
                String tableName = request.queryParams("tname");
                String colQf = request.queryParams("cFamily");

                JSONParser parser = new JSONParser();
                try {

                    Object obj = parser.parse(colQf);

                    JSONObject jsonObject = (JSONObject) obj;

                    String name = (String) jsonObject.get("table_name");
                    System.out.println(name);

                    JSONArray msg = (JSONArray) jsonObject.get("column_family");
                    HBaseCRUD hr = new HBaseCRUD();
                    hr.create_Table(name,msg);
                    return "Successfully created table "+tableName;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return "Unable to create table"+tableName;
                }
            }
        });

    }

    private static Configuration configureFreemarker() {
        Configuration cfg = new Configuration();

        try {

            //indicates the templates directory to freemarker
            cfg.setDirectoryForTemplateLoading(new File("templates"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    //uses to create a Hashmap with specific keys
    private static HashMap getPageParams(String page, String title) {
        HashMap params = new HashMap();

        //page and title from main.ftl
        params.put("page", "pages/" + page);
        params.put("title", title);
        return params;
    }

}

