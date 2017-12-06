package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Get list of commits for current PushEvent
            JSONArray pushEvents = event.getJSONObject("payload").getJSONArray("commits");

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");
            // Add formatted date
            sb.append(" on ");
            sb.append(formatted);
            sb.append("<br />");
            // Add list of commits with their SHA and commit message
            sb.append("Commits:");
            sb.append("<br />");
            JSONObject pushEvent;
            for (int j = 0; j < pushEvents.length(); j++) {
                pushEvent = pushEvents.getJSONObject(j);
                sb.append(pushEvent.getString("sha").substring(0, 8));
                sb.append(": ");
                sb.append(pushEvent.getString("message"));
                sb.append("<br />");
            }
            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String baseUrl = BASE_URL + user + "/events";
        int page = 1;
        int pushEventCount = 0;
        // while there are more pages being returned and we have not received 10 pushEvents yet
        while (true) {
            String paginatedURL = baseUrl + "?page=" + String.valueOf(page);
            System.out.println(paginatedURL);
            JSONObject json = Util.queryAPI(new URL(paginatedURL));
            System.out.println(json);
            JSONArray events = json.getJSONArray("root");
            // if nothing is returned, stop getting events
            if (events.length() == 0) {
                break;
            }
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.getJSONObject(i);
                // Get event type
                String type = event.getString("type");
                // Get push events only
                if (!type.equals("PushEvent")) {
                    continue;
                }
                eventList.add(event);
                pushEventCount++;
                if (pushEventCount == 10) {
                    return eventList;
                }
            }
            page++;
        }
        return eventList;
    }
}