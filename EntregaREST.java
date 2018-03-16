package vertx;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;


public class EntregaREST extends AbstractVerticle {
	
	Map<Integer,Lluvia> tablaLluvia;
	Map<Integer,Luminosidad> tablaLum;
	Map<Integer,Toldo> tablaToldo;
	Map<Integer,Temperatura> tablaTemp;
	
	private SQLClient mySQLClient;
	private String nomBaseDatosLluvia = "lluvia";
	private String nomBaseDatosLum = "luminosidad";
	private String nomBaseDatosToldo = "toldo";
	private String nomBaseDatosTemp = "temperatura";
	
	
	
	
	public void start(Future<Void> startFuture) {

		
		tablaLluvia = new HashMap<>();
		tablaLum= new HashMap<>();
		tablaTemp = new HashMap<>();
		tablaToldo = new HashMap<>();
		
		JsonObject mySQLClientConfig = new JsonObject()
				.put("host","127.0.0.1")
				.put("port", 3306) 
				.put("database","toldo")
				.put("username", "root")
				.put("password", "root");
		
		mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
		
		Router router = Router.router(vertx);
		vertx.createHttpServer().requestHandler(router::accept)
			.listen(8083,res->{
				if(res.succeeded()) {
					System.out.println("Servidor desplegado");
				} else {
					System.out.println("Error: " + res.cause());
				}
			}); 
		
		router.route("/restAPI/Proyecto/temperatura").handler(BodyHandler.create());
		router.route("/restAPI/Proyecto/lluvia").handler(BodyHandler.create());
		router.route("/restAPI/Proyecto/toldo").handler(BodyHandler.create());
		router.route("/restAPI/Proyecto/luminosidad").handler(BodyHandler.create());
		
		router.get("/restAPI/Proyecto/temperatura").handler(this::getAllTemp);
		router.get("/restAPI/Proyecto/lluvia").handler(this::getAllLluvia);
		router.get("/restAPI/Proyecto/toldo").handler(this::getAllToldo);
		router.get("/restAPI/Proyecto/luminosidad").handler(this::getAllLum);
		
		router.get("/restAPI/Proyecto/temperatura/:idFilter").handler(this::getOneTemp);
		router.get("/restAPI/Proyecto/lluvia/:idFilter").handler(this::getOneLluvia);
		router.get("/restAPI/Proyecto/toldo/:idFilter").handler(this::getOneToldo);
		router.get("/restAPI/Proyecto/luminosidad/:idFilter").handler(this::getOneLum);
			
		router.put("/restAPI/Proyecto/lluvia").handler(this::putOneLluvia);
		router.put("/restAPI/Proyecto/temperatura").handler(this::putOneTemp);
		router.put("/restAPI/Proyecto/toldo").handler(this::putOneToldo);
		router.put("/restAPI/Proyecto/luminosidad").handler(this::putOneLum);
		
		//tablaTemp.put(1,new Temperatura(1,0,0));
	}
	
	///////////////////////////////////////////////////////////////////
	///////////////// GET ALL /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	private void getAllTemp(RoutingContext routingContext) {
		routingContext.response().setStatusCode(200)
			.putHeader("content-type", "type=application/json; charset=utf-8")
			.end(Json.encodePrettily(tablaTemp.values()));
	}
	
	private void getAllLluvia(RoutingContext routingContext) {
		routingContext.response().setStatusCode(200)
			.putHeader("content-type", "type=application/json; charset=utf-8")
			.end(Json.encodePrettily(tablaLluvia.values()));
	}
	
	private void getAllToldo(RoutingContext routingContext) {
		routingContext.response().setStatusCode(200)
			.putHeader("content-type", "type=application/json; charset=utf-8")
			.end(Json.encodePrettily(tablaToldo.values()));
	}
	
	private void getAllLum(RoutingContext routingContext) {
		routingContext.response().setStatusCode(200)
			.putHeader("content-type", "type=application/json; charset=utf-8")
			.end(Json.encodePrettily(tablaLum.values()));
	}
	
	
	///////////////////////////////////////////////////////////////////
	///////////////// GET ONE /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////	
	private void getOneTemp(RoutingContext routingContext){
		String paramStr= routingContext.request().getParam("idFilter");
		if(paramStr!=null){
			try{
		int param= Integer.parseInt(paramStr);
		
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "SELECT idTemp,valor,date " 
						+ "FROM " + nomBaseDatosTemp + " "
						+ "WHERE idTemp = ?";
				
				JsonArray paramQuery  = new JsonArray().add(param);
				connection.queryWithParams(query, paramQuery, res ->{
					if(res.succeeded()){
						routingContext.response().end(Json.encodePrettily
								(res.result().getRows().get(0)));
					}else{
						routingContext.response().setStatusCode(400).end(res.cause().toString());
					}
				});
				
			} else{
				routingContext.response().setStatusCode(400).end(conn.cause().toString());
			}
		});
		
		
		
		routingContext.response().setStatusCode(200)
		.end(Json.encodePrettily(tablaTemp.get(param)));
		} catch (ClassCastException e){
			routingContext.response().setStatusCode(400).end();
		}} else{
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void getOneToldo(RoutingContext routingContext){
		String paramStr= routingContext.request().getParam("idFilter");
		if(paramStr!=null){
			try{
		int param= Integer.parseInt(paramStr);
		
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "SELECT idToldo,state, date " 
						+ "FROM " + nomBaseDatosToldo  + " "
						+ "WHERE idToldo = ?";
				
				JsonArray paramQuery  = new JsonArray().add(param);
				connection.queryWithParams(query, paramQuery, res ->{
					if(res.succeeded()){
						routingContext.response().end(Json.encodePrettily
								(res.result().getRows().get(0)));
					}else{
						routingContext.response().setStatusCode(400).end(res.cause().toString());
					}
				});
				
			} else{
				routingContext.response().setStatusCode(400).end(conn.cause().toString());
			}
		});
		
		
		
		routingContext.response().setStatusCode(200)
		.end(Json.encodePrettily(tablaToldo.get(param)));
		} catch (ClassCastException e){
			routingContext.response().setStatusCode(400).end();
		}} else{
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	
	
	private void getOneLum(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("idFilter");
		if(paramStr != null) {
			try{
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn->{
					if(conn.succeeded()) {
						SQLConnection connection = conn.result();
						String query = "SELECT idLumin, valor, date " 
								+ "FROM " + nomBaseDatosLum + " "
								+ "WHERE idLumin = ?";
						JsonArray paramQuery = new JsonArray()
								.add(param);
						connection.queryWithParams(
								query,
								paramQuery,
								res->{
									if(res.succeeded()) {
										routingContext.response().end(Json.encodePrettily(res.result().getRows()));
									}else {
										routingContext.response().setStatusCode(400).end(
												"Error: " + res.cause());
									}
					
								});
						
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + conn.cause());
					}
				});
				routingContext.response().setStatusCode(200)
					.end(Json.encodePrettily(tablaLum.get(param)));
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}			
		}else {
			routingContext.response().setStatusCode(400).end();
		}
		
		
	}
	
	private void getOneLluvia(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("idFilter");
		if(paramStr != null) {
			try{
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn->{
					if(conn.succeeded()) {
						SQLConnection connection = conn.result();
						String query = "SELECT idLluvia, state, date " 
								+ "FROM " + nomBaseDatosLluvia + " "
								+ "WHERE idLluvia = ?";
						JsonArray paramQuery = new JsonArray()
								.add(param);
						connection.queryWithParams(
								query,
								paramQuery,
								res->{
									if(res.succeeded()) {
										routingContext.response().end(Json.encodePrettily(res.result().getRows()));
									}else {
										routingContext.response().setStatusCode(400).end(
												"Error: " + res.cause());
									}
					
								});
						
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + conn.cause());
					}
				});
				routingContext.response().setStatusCode(200)
					.end(Json.encodePrettily(tablaLluvia.get(param)));
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}			
		}else {
			routingContext.response().setStatusCode(400).end();
		}
		
		
	}
	
	

	///////////////////////////////////////////////////////////////////
	///////////////// PUT ONE /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	
	
	private void putOneLluvia(RoutingContext routingContext){
		Lluvia state= Json.decodeValue(routingContext.getBody(), Lluvia.class);
		tablaLluvia.put(state.getId(), state);
		
		routingContext.response().setStatusCode(201)
		.putHeader("content-type","type=application/json; charset=utf-8")
		.end(Json.encodePrettily(state));
		
	}
	
	private void putOneTemp(RoutingContext routingContext){
		Temperatura state= Json.decodeValue(routingContext.getBody(), Temperatura.class);
		tablaTemp.put(state.getId(), state);
		
		routingContext.response().setStatusCode(201)
		.putHeader("content-type","type=application/json; charset=utf-8")
		.end(Json.encodePrettily(state));
		
	}
	
	private void putOneLum(RoutingContext routingContext){
		Luminosidad state= Json.decodeValue(routingContext.getBody(), Luminosidad.class);
		tablaLum.put(state.getId(), state);
		
		routingContext.response().setStatusCode(201)
		.putHeader("content-type","type=application/json; charset=utf-8")
		.end(Json.encodePrettily(state));
		
	}
	
	private void putOneToldo(RoutingContext routingContext){
		Toldo state= Json.decodeValue(routingContext.getBody(), Toldo.class);
		tablaToldo.put(state.getId(), state);
		
		routingContext.response().setStatusCode(201)
		.putHeader("content-type","type=application/json; charset=utf-8")
		.end(Json.encodePrettily(state));
		
	}
}