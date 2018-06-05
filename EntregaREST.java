package vertx;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.messages.MqttPublishMessage;


public class EntregaREST extends AbstractVerticle {
	
	private SQLClient mySQLClient;

	private static Multimap<String,MqttEndpoint> clientTopics;
	
	
	public void start(Future<Void> startFuture) {
		clientTopics = HashMultimap.create();
		
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
		router.route("/restAPI/Proyecto/humedad").handler(BodyHandler.create());
		router.route("/restAPI/Proyecto/dispositivo").handler(BodyHandler.create());
		
		router.get("/restAPI/Proyecto/temperatura").handler(this::getAllTemp);
		router.get("/restAPI/Proyecto/lluvia").handler(this::getAllLluvia);
		router.get("/restAPI/Proyecto/toldo").handler(this::getAllToldo);
		router.get("/restAPI/Proyecto/humedad").handler(this::getAllHum);
		router.get("/restAPI/Proyecto/dispositivo").handler(this::getAllDisp);
		
		router.get("/restAPI/Proyecto/temperatura/:idFilter").handler(this::getOneTemp);
		router.get("/restAPI/Proyecto/lluvia/:idFilter").handler(this::getOneLluvia);
		router.get("/restAPI/Proyecto/toldo/:idFilter").handler(this::getOneToldo);
		router.get("/restAPI/Proyecto/humedad/:idFilter").handler(this::getOneHum);
		router.get("/restAPI/Proyecto/dispositivo/:idFilter").handler(this::getOneDisp);
		
		router.post("/restAPI/Proyecto/lluvia").handler(this::putOneLluvia);
		router.post("/restAPI/Proyecto/toldo").handler(this::putOneToldo);
		router.post("/restAPI/Proyecto/humedad").handler(this::putOneHum);
		router.post("/restAPI/Proyecto/dispositivo").handler(this::putOneDisp);
		router.post("/restAPI/Proyecto/temperatura").handler(this::putOneTemp);

	
		
		
		MqttServer mqttServer = MqttServer.create(vertx);
		initialize(mqttServer);
		
		MqttClient mqttClient = MqttClient.create(vertx,
				new MqttClientOptions().setAutoKeepAlive(true));
		// Aqui habria que modificar localhost por la IP o la URL del equipo en que está el cliente
		mqttClient.connect(1883, "localhost", handler ->{
			mqttClient.subscribe("topic_2",MqttQoS.AT_LEAST_ONCE.value(), msg -> {
				if(msg.succeeded()) {
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");
				}
			});
			
			new Timer().scheduleAtFixedRate(new TimerTask() {
				public void run() {
					// Publicamos un mensaje en el topic "topic_2" con el contenido "Ejemplo" y la hora.
					// Ajustamos el QoS para que se entregue al menos una vez. Indicamos que el
					// mensaje NO es un duplicado (false) y que NO debe ser retenido en el canal
					// (false)
					mqttClient.publish("topic_2", Buffer.buffer("Ejemplo a las " + Calendar.getInstance().getTime().toString()), MqttQoS.AT_LEAST_ONCE, false, false);
				}
			}, 1000, 3000);
		
		});
			
		
		// Ahora creamos un segundo cliente, al que se supone deben llegar todos los mensajes que el
				// cliente 1 desplegado anteriormente publique en el topic "topic_2". Este era el punto en el 
				// que el proyecto anterior fallaba, debido a que no exist�a ning�n broken que se encargara
				// de realizar el env�o desde el servidor al resto de clientes.
				MqttClient mqttClient2 = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
				mqttClient2.connect(1883, "localhost", s -> {

					// Al igual que antes, este cliente se suscribe al topic_2 para poder recibir los mensajes
					// que el cliente 1 env�e a trav�s de MQTT.
					mqttClient2.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
						if (handler.succeeded()) {
							// En este punto, el cliente 2 tambi�n est� suscrito al servidor, por lo que ya podr�
							// empezar a recibir los mensajes publicados en el topic.
							System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal topic_2");
							
							// Adem�s de suscribirnos al servidor, registraremos un manejador para interceptar los mensajes
							// que lleguen a nuestro cliente. De manera que el proceso ser�a el siguiente:
							// El cliente 1 env�a un mensaje al servidor -> el servidor lo recibe y busca los clientes suscritos
							//     al topic -> el servidor reenv�a el mensaje a esos clientes -> los clientes (en este caso
							//     el cliente 2) recibe el mensaje y lo proceso si fuera necesario.
							mqttClient2.publishHandler(new Handler<MqttPublishMessage>() {
								@Override 
								public void handle(MqttPublishMessage arg0) {
									// Si se ejecuta este c�digo es que el cliente 2 ha recibido un mensaje publicado
									// en alg�n topic al que estaba suscrito (en este caso, al topic_2).
									System.out.println("Mensaje recibido por el cliente 2: " + arg0.payload().toString());							
								}
							});
						}
					});

				});
		/*	mqttClient.publish("topic_1", Buffer.buffer(""  
							+ "{"
							+ "'clientID' : 234234234"
							+ "'action': 'activate'"
							+ "}"), MqttQoS.AT_LEAST_ONCE, false, false); // Estos tres últimos son siempre igual
		});*/
	}
	public void initialize(MqttServer mqttServer) {
		mqttServer.endpointHandler(endpoint -> {
			// Si se ejecuta este c�digo es que un cliente se ha suscrito al servidor MQTT para 
			// alg�n topic.
			System.out.println("Nuevo cliente MQTT [" + endpoint.clientIdentifier()
					+ "] solicitando suscribirse [Id de sesi�n: " + endpoint.isCleanSession() + "]");
			// Indicamos al cliente que se ha contectado al servidor MQTT y que no ten�a
			// sesi�n previamente creada (par�metro false)
			endpoint.accept(false);

			// Handler para gestionar las suscripciones a un determinado topic. Aqu� registraremos
			// el cliente para poder reenviar todos los mensajes que se publicen en el topic al que
			// se ha suscrito.
			handleSubscription(endpoint);

			// Handler para gestionar las desuscripciones de un determinado topic. Haremos lo contrario
			// que el punto anterior para eliminar al cliente de la lista de clientes registrados en el 
			// topic. De este modo, no seguir� recibiendo mensajes en este topic.
			handleUnsubscription(endpoint);

			// Este handler ser� llamado cuando se publique un mensaje por parte del cliente en alg�n
			// topic creado en el servidor MQTT. En esta funci�n obtendremos todos los clientes
			// suscritos a este topic y reenviaremos el mensaje a cada uno de ellos. Esta es la tarea
			// principal del broken MQTT. En este caso hemos implementado un broker muy muy sencillo. 
			// Para gestionar QoS, asegurar la entregar, guardar los mensajes en una BBDD para despu�s
			// entregarlos, guardar los clientes en caso de ca�da del servidor, etc. debemos recurrir
			// a un c�digo m�s elaborado o usar una soluci�n existente como por ejemplo Mosquitto.
			publishHandler(endpoint);

			// Handler encargado de gestionar las desconexiones de los clientes al servidor. En este caso
			// eliminaremos al cliente de todos los topics a los que estuviera suscrito.
			handleClientDisconnect(endpoint);
		}).listen(ar -> {
			if (ar.succeeded()) {
				System.out.println("MQTT server esta a la escucha por el puerto " + ar.result().actualPort());
			} else {
				System.out.println("Error desplegando el MQTT server");
				ar.cause().printStackTrace();
			}
		});
	}
	
	

	protected void handleSubscription(MqttEndpoint endpoint) {
		// Este método no se toca en principio, aunque se podría modificar algún parámetro para restringir el acceso a ciertos canales.
		endpoint.subscribeHandler(subscribe -> {
			// Los niveles de QoS permiten saber el tipo de entrega que se realizar�:
			// - AT_LEAST_ONCE: Se asegura que los mensajes llegan a los clientes, pero no
			// que se haga una �nica vez (pueden llegar duplicados)
			// - EXACTLY_ONCE: Se asegura que los mensajes llegan a los clientes un �nica
			// vez (mecanismo m�s costoso)
			// - AT_MOST_ONCE: No se asegura que el mensaje llegue al cliente, por lo que no
			// es necesario ACK por parte de �ste
			List<MqttQoS> grantedQosLevels = new ArrayList<>();
			for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
				System.out.println("Suscripcion al topic " + s.topicName() + " con QoS " + s.qualityOfService());
				grantedQosLevels.add(s.qualityOfService());
				
				// A�adimos al cliente en la lista de clientes suscritos al topic
				clientTopics.put(s.topicName(), endpoint);
			}
		
			// Enviamos el ACK al cliente de que se ha suscrito al topic con los niveles de
			// QoS indicados
			endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
		});
	}
	
	protected void handleUnsubscription(MqttEndpoint endpoint) {
		// Otro método que no se toca
		endpoint.unsubscribeHandler(unsubscribe->{
			for (String topic : unsubscribe.topics()) {
				System.out.println("El cliente " + 
					endpoint.clientIdentifier()
					+ "ha eliminado la subscripcion del canal " +
					topic);
			}
			endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
		});
		
	}
	
	private static void publishHandler(MqttEndpoint endpoint) {
		endpoint.publishHandler(message -> {
			// Suscribimos un handler cuando se solicite una publicaci�n de un mensaje en un
			// topic
			handleMessage(message, endpoint);
		}).publishReleaseHandler(messageId -> {
			// Suscribimos un handler cuando haya finalizado la publicaci�n del mensaje en
			// el topic
			endpoint.publishComplete(messageId);
		});
	}
	
	private static void handleMessage(MqttPublishMessage message, MqttEndpoint endpoint) {
		System.out.println("Mensaje publicado por el cliente " + endpoint.clientIdentifier() + " en el topic "
				+ message.topicName());
		System.out.println("    Contenido del mensaje: " + message.payload().toString());
		
		// Obtenemos todos los clientes suscritos a ese topic (exceptuando el cliente que env�a el 
		// mensaje) para as� poder reenviar el mensaje a cada uno de ellos. Es aqu� donde nuestro
		// c�digo realiza las funciones de un broken MQTT
		System.out.println("Origen: " + endpoint.clientIdentifier());
		for (MqttEndpoint client: clientTopics.get(message.topicName())) {
			System.out.println("Destino: " + client.clientIdentifier());
			if (!client.clientIdentifier().equals(endpoint.clientIdentifier()))
				client.publish(message.topicName(), message.payload(), message.qosLevel(), message.isDup(), message.isRetain());
		}
		
		if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			String topicName = message.topicName();
			switch (topicName) {
			// Se podr�a hacer algo con el mensaje como, por ejemplo, almacenar un registro
			// en la base de datos
			}
			// Env�a el ACK al cliente de que el mensaje ha sido publicado
			endpoint.publishAcknowledge(message.messageId());
		} else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
			// Env�a el ACK al cliente de que el mensaje ha sido publicado y cierra el canal
			// para este mensaje. As� se evita que los mensajes se publiquen por duplicado
			// (QoS)
			endpoint.publishRelease(message.messageId());
		}
	}


	
	protected void handleClientDisconnect(MqttEndpoint endpoint) {
		// Lo mismo
		endpoint.disconnectHandler(h -> {
			// Eliminamos al cliente de todos los topics a los que estaba suscritos
			Stream.of(clientTopics.keySet())
				.filter(e -> clientTopics.containsEntry(e, endpoint))
				.forEach(s -> clientTopics.remove(s, endpoint));
			System.out.println("El cliente remoto se ha desconectado [" + endpoint.clientIdentifier() + "]");
		});
	}
	
	

	

	///////////////////////////////////////////////////////////////////
	///////////////// GET ALL /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	private void getAllTemp(RoutingContext routingContext) {
	
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM toldo.temperatura;", new JsonArray(), res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
									.end(Json.encodePrettily(res.result().getRows()));
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} catch (Exception e) {
					routingContext.response().setStatusCode(400).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});

	}
	
	private void getAllLluvia(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM toldo.lluvia;", new JsonArray(), res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(res.result().getRows()));
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} catch (Exception e) {
					routingContext.response().setStatusCode(400).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		
	}
	
	private void getAllToldo(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM toldo.toldo;", new JsonArray(), res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(res.result().getRows()));
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} catch (Exception e) {
					routingContext.response().setStatusCode(400).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		}
	
	private void getAllHum(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM toldo.humedad;", new JsonArray(), res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(res.result().getRows()));
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} catch (Exception e) {
					routingContext.response().setStatusCode(400).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	
	}
	
	private void getAllDisp(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					conn.result().queryWithParams("SELECT * FROM toldo.dispositivo;", new JsonArray(), res -> {
						if (res.succeeded()) {
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
							.end(Json.encodePrettily(res.result().getRows()));
						} else {
							routingContext.response().setStatusCode(401).end();
						}
					});
				} catch (Exception e) {
					routingContext.response().setStatusCode(400).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
	
	}
	
	
	
	///////////////////////////////////////////////////////////////////
	///////////////// GET ONE /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////	
	private void getOneTemp(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					int id = Integer.parseInt(routingContext.request().getParam("idFilter"));
					conn.result().queryWithParams("SELECT * FROM toldo.temperatura WHERE idTemp=?;",
							new JsonArray().add(id), res -> {
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									routingContext.response()
											.setStatusCode(200)
											.end(Json.encodePrettily(resultSet.getRows().get(0)));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
				} catch (Exception e) {
					routingContext.response().setStatusCode(401).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		


	}
	
	private void getOneToldo(RoutingContext routingContext){
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					int id = Integer.parseInt(routingContext.request().getParam("idFilter"));
					conn.result().queryWithParams("SELECT * FROM toldo.toldo WHERE idToldo=?;",
							new JsonArray().add(id), res -> {
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									routingContext.response()
											.setStatusCode(200)
											.end(Json.encodePrettily(resultSet.getRows().get(0)));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
				} catch (Exception e) {
					routingContext.response().setStatusCode(401).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
	});
		
		

	}
	
	
	
	
	private void getOneHum(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					int id = Integer.parseInt(routingContext.request().getParam("idFilter"));
					conn.result().queryWithParams("SELECT * FROM toldo.humedad WHERE idHumedad=?;",
							new JsonArray().add(id), res -> {
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									routingContext.response()
											.setStatusCode(200)
											.end(Json.encodePrettily(resultSet.getRows().get(0)));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
				} catch (Exception e) {
					routingContext.response().setStatusCode(401).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		
	

	}
	
	private void getOneLluvia(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					int id = Integer.parseInt(routingContext.request().getParam("idFilter"));
					conn.result().queryWithParams("SELECT * FROM toldo.lluvia WHERE idLluvia=?;",
							new JsonArray().add(id), res -> {
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									routingContext.response()
											.setStatusCode(200)
											.end(Json.encodePrettily(resultSet.getRows().get(0)));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
				} catch (Exception e) {
					routingContext.response().setStatusCode(401).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		
	

	}
	
	
	private void getOneDisp(RoutingContext routingContext) {
		mySQLClient.getConnection(conn -> {
			if (conn.succeeded()) {
				try {
					int id = Integer.parseInt(routingContext.request().getParam("idFilter"));
					conn.result().queryWithParams("SELECT * FROM toldo.dispositivo WHERE idDispositivo=?;",
							new JsonArray().add(id), res -> {
								if (res.succeeded()) {
									ResultSet resultSet = res.result();
									routingContext.response()
											.setStatusCode(200)
											.end(Json.encodePrettily(resultSet.getRows().get(0)));
								} else {
									routingContext.response().setStatusCode(401).end();
								}
							});
				} catch (Exception e) {
					routingContext.response().setStatusCode(401).end();
				}
			} else {
				routingContext.response().setStatusCode(401).end();
			}
		});
		
	

	}

	///////////////////////////////////////////////////////////////////
	///////////////// PUT ONE /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	
	
	private void putOneLluvia(RoutingContext routingContext){
	
		Date fecha = new Date();
		System.out.println (fecha);
		
		Lluvia state= Json.decodeValue(routingContext.getBody(), Lluvia.class);
		int id = state.getId();
		long date = fecha.getTime();
		boolean estado = state.isState();
		int idDis = state.getIdDispositivo();
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				
				String query = "INSERT INTO toldo.lluvia(state,date,idDispositivo) " 
							   + " VALUES (?,?,?)"; 
				connection.queryWithParams(query, new JsonArray().add(estado).add(date).add(idDis),res ->{
					if(res.succeeded()) {
						//routingContext.response().setStatusCode(200).end("Todo correcto");
						routingContext.response().end(Json.encodePrettily(res.result().getRows()));
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + res.cause());
					}
				});
				connection.close();
				
			}
		});
		
	}
	
	private void putOneTemp(RoutingContext routingContext){
		
		Date fecha = new Date();
		System.out.println (fecha);
		
		
		Temperatura state = Json.decodeValue(routingContext.getBody(), Temperatura.class);
		int id = state.getIdTemp();
		float val = state.getValor();
		long dat = fecha.getTime();	//Te devuelve la hora en un long
		int idDis = state.getIdDispositivo();
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "INSERT INTO temperatura(valor,date,idDispositivo) " 
							   + " VALUES (?,?,?)"; 
				connection.queryWithParams(query, new JsonArray()./*add(id).*/add(val).add(dat).add(idDis),res ->{
					if(res.succeeded()) {
						//routingContext.response().setStatusCode(200).end("Todo correcto");
						routingContext.response().end(Json.encodePrettily(res.result().getRows()));
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + res.cause());
					}
				});
				
				connection.close();
			}
		});
		
		
		
		
	}
	
	private void putOneHum(RoutingContext routingContext){

		Date fecha = new Date();
		System.out.println (fecha);
		
		
		 Humedad state= Json.decodeValue(routingContext.getBody(), Humedad.class);
		int id = state.getId();
		long date = fecha.getTime();
		float valor = state.getValue();
		int idDis = state.getIdDispositivo();
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "INSERT INTO toldo.humedad(valor,date,idDispositivo) " 
							   + " VALUES (?,?,?)"; 
				connection.queryWithParams(query, new JsonArray().add(valor).add(date).add(idDis),res ->{
					if(res.succeeded()) {
						//routingContext.response().setStatusCode(200).end("Todo correcto");
						routingContext.response().end(Json.encodePrettily(res.result().getRows()));
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + res.cause());
					}
				});
				
				connection.close();
			}
		});
		
	}
	
	private void putOneToldo(RoutingContext routingContext){
		Date fecha = new Date();
		System.out.println (fecha);
		
		Toldo state= Json.decodeValue(routingContext.getBody(), Toldo.class);
		int id = state.getId();
		long date = fecha.getTime();
		boolean estado = state.isState();
		int idDis = state.getIdDispositivo();
		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "INSERT INTO toldo(state,date,idDispositivo) " 
							   + " VALUES (?,?,?)"; 
				connection.queryWithParams(query, new JsonArray().add(estado).add(date).add(idDis),res ->{
					if(res.succeeded()) {
						//routingContext.response().setStatusCode(200).end("Todo correcto");
						routingContext.response().end(Json.encodePrettily(res.result().getRows()));
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + res.cause());
					}
				});
				
				connection.close();
			}
		});
		
	}
	
		
	
	
	private void putOneDisp(RoutingContext routingContext){

		Dispositivo state= Json.decodeValue(routingContext.getBody(), Dispositivo.class);
		int id = state.getIdDispositivo();
		String alias = state.getAlias();
		String dirIP = state.getDirIP();

		mySQLClient.getConnection(conn ->{
			if(conn.succeeded()){
				SQLConnection connection = conn.result();
				String query = "INSERT INTO toldo.dispositivo(idDispositivo,alias,dirIP) " 
							   + " VALUES (?,?,?)"; 
				connection.queryWithParams(query, new JsonArray().add(id).add(alias).add(dirIP),res ->{
					if(res.succeeded()) {
						//routingContext.response().setStatusCode(200).end("Todo correcto");
						routingContext.response().end(Json.encodePrettily(res.result().getRows()));
					}else {
						routingContext.response().setStatusCode(400).end(
								"Error: " + res.cause());
					}
				});
				
				connection.close();
			}
		});

	}
	
	}
