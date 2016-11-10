import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class getCoordinates {
    public static void main(String[] args) throws IOException, ParseException {

        Socket s = new Socket("gis.sofiyskavoda.bg", 6080);
        String headers = "GET /arcgis/rest/services/InfrastructureAlerts/MapServer/2/query?d=1478166031551&f=json&where=ACTIVESTATUS%20%3D%20%27In%20Progress%27&returnGeometry=true&spatialRel=esriSpatialRelIntersects&outFields=*&outSR=102100 HTTP/1.1\n" +
                "Host: gis.sofiyskavoda.bg:6080\n" +
                "Connection: keep-alive\n" +
                "Origin: http://infocenter.sofiyskavoda.bg\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Accept: */*\n" +
                "Referer: http://infocenter.sofiyskavoda.bg/default.aspx?url=http://www.sofiyskavoda.bg/water_stops.aspx&e=&d=649\n" +
                "Accept-Language: bg-BG,bg;q=0.8";

        PrintWriter pw = new PrintWriter(s.getOutputStream());
        pw.println(headers + "\n");
        pw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String responseLine, response = "";
        while((responseLine = br.readLine()) != null) {
            response += responseLine;
            //System.out.println(responseLine);
        }
        br.close();

        String [] obj = (response.split("(\\{.*})"));
        String trash = obj[0];
        response = response.replace(trash ,"");

        //System.out.println(response);
        
        //new
        String [] obj1 = response.split("(\"features.*)");
        String replace = obj1[0];
        response = "{"+response.replace(replace ,"");

        JSONParser jsonParse = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParse.parse(response);

        JSONArray features = (JSONArray) jsonObject.get("features");
        if(features.size() == 0) {
            System.out.println("В момента няма текущи спирания.");
        }else {
            for (Object featuresData : features) {
                String str = featuresData.toString();
                JSONObject jsonObj = (JSONObject) jsonParse.parse(str);

                String attributes = jsonObj.get("attributes").toString();
                JSONObject jsonAttr = (JSONObject) jsonParse.parse(attributes);

                String geometry = jsonObj.get("geometry").toString();
                JSONObject jsonGeo = (JSONObject) jsonParse.parse(geometry);

                System.out.println("Тип: " + jsonAttr.get("ALERTTYPE"));
                System.out.println("Описание: " + jsonAttr.get("DESCRIPTION"));
                System.out.println("Местоположение: " + jsonAttr.get("LOCATION"));
                System.out.print("Начало: " + jsonAttr.get("START_H") + ":");
                System.out.println(jsonAttr.get("START_M"));
                System.out.print("Край: " + jsonAttr.get("END_H") + ":");
                System.out.println(jsonAttr.get("END_M"));
                System.out.println("Зона: ");

                JSONArray rings = (JSONArray) jsonGeo.get("rings");
                for (Object ringsData : rings) {
                    JSONArray ringsDataToArr = (JSONArray) ringsData;
                    int pointCounter = 1;

                    for (Object pointsCoordinates : ringsDataToArr) {
                        String pointsCoordToStr = pointsCoordinates.toString();
                        String[] latAndLong = pointsCoordToStr.split(",");

                        double x = Double.parseDouble(latAndLong[0].substring(1));
                        double y = Double.parseDouble(latAndLong[1].substring(0, latAndLong[1].length() - 1));
                        double lat = (180 / Math.PI) * (y / 6378137);
                        double lon = (180 / Math.PI) * (x / 6378137);

                        System.out.println("     Точка" + pointCounter + " (Latitude: " + lat + "  Longitude: " + lon + ")");

                        pointCounter++;
                    }
                }
                System.out.println();
            }
        }
    }
}
