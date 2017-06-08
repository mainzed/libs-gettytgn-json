package tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class getJSON extends HttpServlet {

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
		try {
			// parse params
			String req_uri = "";
			if (request.getParameter("uri") != null) {
				req_uri = request.getParameter("uri");
			}
			String req_format = "";
			if (request.getParameter("format") != null) {
				req_format = request.getParameter("format");
			}
			// parse header
			String acceptHeader = "application/json";
			Enumeration headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = (String) headerNames.nextElement();
				key = key.toLowerCase();
				String value = request.getHeader(key);
				if (key.equals("accept")) {
					System.out.println(key + " " + value);
				}
			}
			req_uri = URLDecoder.decode(req_uri, "UTF-8");
			// call Getty TGN SPARQL endpoint
			String url = "http://vocab.getty.edu/sparql.json";
			String queryString = "prefix ontogeo: <http://www.ontotext.com/owlim/geo#>"
					+ "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"
					+ "prefix skos: <http://www.w3.org/2004/02/skos/core#>"
					+ "prefix tgn: <http://vocab.getty.edu/tgn/>"
					+ "prefix foaf: <http://xmlns.com/foaf/0.1/>"
					+ "prefix gvp: <http://vocab.getty.edu/ontology#>"
					+ "prefix skosxl: <http://www.w3.org/2008/05/skos-xl#>"
					+ "select ?name ?lat ?long ?parentString ?preflabel ?altlabel ?scopenote ?placetypepreferred ?placetypenonpreferred ?id where {"
					+ " ?place gvp:prefLabelGVP [skosxl:literalForm ?name] ."
					+ " ?place foaf:focus [geo:lat ?lat]."
					+ " ?place foaf:focus [geo:long ?long]."
					+ " OPTIONAL{?place gvp:parentString ?parentString .}"
					+ " OPTIONAL{?place skosxl:prefLabel [skosxl:literalForm ?preflabel] .}"
					+ " OPTIONAL{?place skosxl:altLabel [skosxl:literalForm ?altlabel] .}"
					+ " OPTIONAL{?place skos:scopeNote [rdf:value ?scopenote] .}"
					+ " OPTIONAL{?place gvp:placeTypePreferred [gvp:prefLabelGVP [skosxl:literalForm ?placetypepreferred]] }."
					+ " OPTIONAL{?place gvp:placeTypeNonPreferred [gvp:prefLabelGVP [skosxl:literalForm ?placetypenonpreferred]] .}"
					+ " ?place dc:identifier ?id."
					+ " FILTER(?place=<" + req_uri + ">)"
					+ "}";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			String urlParameters = "query=" + queryString;
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			JSONArray bindings = new JSONArray();
			// set out object
			JSONObject gettyTGNObject = new JSONObject();
			JSONArray gettyTGNObject_scopenotes = new JSONArray();
			JSONArray gettyTGNObject_preflabels = new JSONArray();
			JSONArray gettyTGNObject_altlabels = new JSONArray();
			JSONArray gettyTGNObject_playetypepreferred = new JSONArray();
			JSONArray gettyTGNObject_playetypenonpreferred = new JSONArray();
			JSONArray gettyTGNObject_scopenotes2 = new JSONArray();
			JSONArray gettyTGNObject_preflabels2 = new JSONArray();
			JSONArray gettyTGNObject_altlabels2 = new JSONArray();
			JSONArray gettyTGNObject_playetypepreferred2 = new JSONArray();
			JSONArray gettyTGNObject_playetypenonpreferred2 = new JSONArray();
			if (con.getResponseCode() == 200) {
				StringBuilder responseGettyTGN;
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"))) {
					String inputLine;
					responseGettyTGN = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						responseGettyTGN.append(inputLine);
					}
				}
				JSONObject jsonObject = (JSONObject) new JSONParser().parse(responseGettyTGN.toString());
				JSONObject results = (JSONObject) jsonObject.get("results");
				bindings = (JSONArray) results.get("bindings");
				for (Object item : bindings) {
					JSONObject tmp = (JSONObject) item;
					JSONObject tmp_name = (JSONObject) tmp.get("name");
					JSONObject obj_name = new JSONObject();
					obj_name.put("value", tmp_name.get("value"));
					obj_name.put("lang", tmp_name.get("xml:lang"));
					gettyTGNObject.put("name", obj_name);
					JSONObject tmp_lat = (JSONObject) tmp.get("lat");
					gettyTGNObject.put("lat", Double.parseDouble((String) tmp_lat.get("value")));
					JSONObject tmp_long = (JSONObject) tmp.get("long");
					gettyTGNObject.put("long", Double.parseDouble((String) tmp_long.get("value")));
					JSONObject tmp_id = (JSONObject) tmp.get("id");
					gettyTGNObject.put("id", (String) tmp_id.get("value"));
					JSONObject tmp_parentString = (JSONObject) tmp.get("parentString");
					gettyTGNObject.put("parentString", tmp_parentString.get("value"));
					if (!gettyTGNObject_scopenotes.contains(tmp.get("scopenote"))) {
						gettyTGNObject_scopenotes.add((JSONObject) tmp.get("scopenote"));
					}
					if (!gettyTGNObject_preflabels.contains(tmp.get("preflabel"))) {
						gettyTGNObject_preflabels.add((JSONObject) tmp.get("preflabel"));
					}
					if (!gettyTGNObject_altlabels.contains(tmp.get("altlabel"))) {
						gettyTGNObject_altlabels.add((JSONObject) tmp.get("altlabel"));
					}
					if (!gettyTGNObject_playetypepreferred.contains(tmp.get("placetypepreferred"))) {
						gettyTGNObject_playetypepreferred.add((JSONObject) tmp.get("placetypepreferred"));
					}
					if (!gettyTGNObject_playetypenonpreferred.contains(tmp.get("placetypenonpreferred"))) {
						gettyTGNObject_playetypenonpreferred.add((JSONObject) tmp.get("placetypenonpreferred"));
					}
				}
				if (gettyTGNObject_preflabels.get(0) != null) {
					for (Object item : gettyTGNObject_preflabels) {
						JSONObject tmp = (JSONObject) item;
						JSONObject this_obj = new JSONObject();
						this_obj.put("value", tmp.get("value"));
						this_obj.put("lang", tmp.get("xml:lang"));
						gettyTGNObject_preflabels2.add(this_obj);
					}
				}
				if (gettyTGNObject_altlabels.get(0) != null) {
					for (Object item : gettyTGNObject_altlabels) {
						JSONObject tmp = (JSONObject) item;
						JSONObject this_obj = new JSONObject();
						this_obj.put("value", tmp.get("value"));
						this_obj.put("lang", tmp.get("xml:lang"));
						gettyTGNObject_altlabels2.add(this_obj);
					}
				}
				if (gettyTGNObject_scopenotes.get(0) != null) {
					for (Object item : gettyTGNObject_scopenotes) {
						JSONObject tmp = (JSONObject) item;
						JSONObject this_obj = new JSONObject();
						this_obj.put("value", tmp.get("value"));
						this_obj.put("lang", tmp.get("xml:lang"));
						gettyTGNObject_scopenotes2.add(this_obj);
					}
				}
				if (gettyTGNObject_playetypepreferred.get(0) != null) {
					for (Object item : gettyTGNObject_playetypepreferred) {
						JSONObject tmp = (JSONObject) item;
						JSONObject this_obj = new JSONObject();
						this_obj.put("value", tmp.get("value"));
						this_obj.put("lang", tmp.get("xml:lang"));
						gettyTGNObject_playetypepreferred2.add(this_obj);
					}
				}
				if (gettyTGNObject_playetypenonpreferred.get(0) != null) {
					for (Object item : gettyTGNObject_playetypenonpreferred) {
						JSONObject tmp = (JSONObject) item;
						JSONObject this_obj = new JSONObject();
						this_obj.put("value", tmp.get("value"));
						this_obj.put("lang", tmp.get("xml:lang"));
						gettyTGNObject_playetypenonpreferred2.add(this_obj);
					}
				}
				gettyTGNObject.put("scopenotes", gettyTGNObject_scopenotes2);
				gettyTGNObject.put("preflabels", gettyTGNObject_preflabels2);
				gettyTGNObject.put("altlabels", gettyTGNObject_altlabels2);
				gettyTGNObject.put("playetypespreferred", gettyTGNObject_playetypepreferred2);
				gettyTGNObject.put("playetypesnonpreferred", gettyTGNObject_playetypenonpreferred2);
			}
			if (acceptHeader.contains("application/vnd.geo+json") || req_format.equals("geojson")) {
				JSONArray coordinatesArray = new JSONArray();
				JSONObject geometryObject = new JSONObject();
				coordinatesArray.add((double) gettyTGNObject.get("long"));
				coordinatesArray.add((double) gettyTGNObject.get("lat"));
				geometryObject.put("type", "Point");
				geometryObject.put("coordinates", coordinatesArray);
				JSONObject geojson = new JSONObject();
				geojson.put("type", "Feature");
				geojson.put("properties", gettyTGNObject);
				geojson.put("geometry", geometryObject);
				out.print(geojson);
			} else {
				out.print(gettyTGNObject);
			}
		} catch (Exception e) {
			out.print(Logging.getMessageJSON(e, "tools.getJSON"));
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Get JSON of a Getty TGN resource.";
	}

}
