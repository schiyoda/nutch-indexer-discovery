package com.ibm.watson.indexwriter.discovery;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Credentials;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Enrich {

  public class Entity {
    private String text;
    private String type;

    public Entity(String text, String type){
      this.text = text;
      this.type = type;
    }
  }

  public class Annotate {
      private List<Entity> entities;

      Annotate(String text){
        this.entities = new ArrayList<>();
        int num_positive = 0;
        int num_negative = 0;
        String wexhost = "";
        String url1 = "http://" + wexhost + ":8393/api/v10/analysis/text?collection=news&text=";
        String url2 = "&output=application/json";
        String url = url1 + text + url2;
        String credential = Credentials.basic("esadmin", "esadmin");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().header("Authorization", credential).url(url).build();

        try (Response response = client.newCall(request).execute()) {

          JsonObject jsonObj = (JsonObject) new Gson().fromJson(response.body().string(), JsonObject.class);
          JsonObject metadata = jsonObj.getAsJsonObject().get("metadata").getAsJsonObject();
          JsonArray textfacets = metadata.get("textfacets").getAsJsonArray();
          for (int i = 0; i < textfacets.size(); i++) {
            JsonArray path = textfacets.get(i).getAsJsonObject().get("path").getAsJsonArray();
            String keyword = textfacets.get(i).getAsJsonObject().get("keyword").getAsString();

            String path_0 = path.get(0).getAsString();
            if(path_0.equals("_ne")){
              String path_1 = path.get(1).getAsString();
              if(path_1.equals("location")){
                this.entities.add(new Entity(keyword,"Location"));
              }else if(path_1.equals("organization")){
                this.entities.add(new Entity(keyword,"Company"));
              }else if(path_1.equals("person")){
                this.entities.add(new Entity(keyword,"Person"));
              }
            }
          }
        }catch(Exception e){
          System.out.println(e);
        }
      }
  }
}
