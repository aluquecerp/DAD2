// Librerías necesarias
#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>
#include <ESP8266WiFi.h>
#include <RestClient.h>
#include <PubSubClient.h>
#include <Servo.h>
#include <SimpleDHT.h>

// Preparacion de los distintos sensores
int pinDHT11 = 4; //pin para sensor temperatura, D2
int contador=1;
SimpleDHT11 dht11;
Servo servoMotor;

// Configuracion Wifi
WiFiClient espClient;
PubSubClient pubsubClient(espClient);
char msg[50];
const char* serverIP = "192.168.43.3";  	//Haciendo un ipconfig

ESP8266WebServer http_rest_server(8080);
RestClient client = RestClient(serverIP, 8083);

const char* ssid = "AndroidAP1";
const char* password = "123456789";


void callback(char* topic, byte* payload, unsigned int length) {
	Serial.print("Mensaje recibido [");
	Serial.print(topic);
	Serial.print("] ");
	String message = String((char *)payload);
	Serial.print(message);
	Serial.println();

}

void setup() {
  Serial.begin(115200);

	pinMode(2, INPUT); // Pin D4 para el sensor de lluvia
	pinMode(16, OUTPUT); // Pin para el LED que representa el motor	


// Inicio de la conexion
  Serial.println();
  Serial.print("Conectando a ");
  Serial.println(ssid);

// Modo cliente
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.print("Red conectada. Dirección IP: ");
  Serial.println(WiFi.localIP());

  pubsubClient.setServer(serverIP, 1883);
  pubsubClient.setCallback(callback);


}

void reconnect() {
	while (!pubsubClient.connected()) {
		Serial.print("Conectando al servidor MQTT\n");
		if (pubsubClient.connect("ESP8266Client")) {
			Serial.println("Conectado\n");
			pubsubClient.publish("topic_2", "Hola a todos");
			pubsubClient.subscribe("topic_1");
		} else {
			Serial.print(" Error, rc= ");
			Serial.print(pubsubClient.state());
			Serial.println(" Reintentando en 5 segundos ");
			delay(5000);
		}
	}
}

void loop() {


	// MQTT
  if (!pubsubClient.connected()) {
    reconnect();
  }

  pubsubClient.loop();


//Inicializacion sensor temperatura/humedad
	byte temperature = 0;
	byte humidity = 0;
	int err = SimpleDHTErrSuccess;
	if ((err = dht11.read(pinDHT11, &temperature, &humidity, NULL)) != SimpleDHTErrSuccess) {
		Serial.print("Read DHT11 failed, err="); Serial.println(err);delay(1000);
		return;
	}

//Inicializacion de los JsonBuffer y cadenas que enviaremos
  String responseTemp = "";
  String responseLluvia = "";
	String responseHum = "";
  String responseToldo = "";
	  String responseDisp = "";


//En principio como no vamos a hacer ningun get, los root los comentamos
 const size_t capacityTemp = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 60;
  DynamicJsonBuffer jsonBuffer(capacityTemp);
  //JsonObject& rootTemp = jsonBuffer.parseObject(responseTemp);


 const size_t capacityLluvia = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 60;
 DynamicJsonBuffer jsonBufferLluvia(capacityLluvia);
 //JsonObject& rootLluvia = jsonBufferLluvia.parseObject(responseLluvia);


 const size_t capacityHum = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 60;
  DynamicJsonBuffer jsonBufferHum(capacityHum);
  //JsonObject& rootHum = jsonBufferHum.parseObject(responseHum);


const size_t capacityToldo = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 60;
DynamicJsonBuffer jsonBufferToldo(capacityToldo);
//JsonObject& rootToldo = jsonBufferToldo.parseObject(responseToldo);

const size_t capacityDisp = JSON_OBJECT_SIZE(3) + JSON_ARRAY_SIZE(2) + 60;
 DynamicJsonBuffer jsonBufferDisp(capacityDisp);
 //JsonObject& rootDisp = jsonBufferDisp.parseObject(responseDisp);


 /**********************************************
 *********************DISPOSITIVO*********************
 ***********************************************/

if(contador >0){
	// Post para almacenar en la base de datos nuestro dispositivo
 JsonObject& disp = jsonBufferDisp.createObject();
 disp["idDispositivo"] = 1;
 disp["alias"] ="Disp 1" ;
 disp["dirIP"] = serverIP; //serverIP
 char dispStr[100];
 disp.printTo(dispStr);
 Serial.println("Se enviaran los siguientes datos del dispositivo : \n");
 Serial.println(dispStr);
Serial.println("\n");
 int statusCodeDisp = client.post("/restAPI/Proyecto/dispositivo/", dispStr, &responseDisp);
Serial.println(statusCodeDisp);
contador= 0;
}

/*****************************************************
************************** TEMPERATURA **************************
******************************************************************/

	// Post para temperatura

  JsonObject& temperatura = jsonBuffer.createObject();

  temperatura["valor"] = (float) temperature;
	temperatura["date"] = 0;
  temperatura["idDispositivo"] = 1;
  char temperaturaStr[100];
  temperatura.printTo(temperaturaStr);
	Serial.println("Se enviaran los datos de temperatura : \n");
	Serial.println(temperaturaStr);
		Serial.println("\n");
	int	statusCodeTemp = client.post("/restAPI/Proyecto/temperatura", temperaturaStr, &responseTemp);


	/*********************************************************************
***************************************	LLUVIA***************************************
	*****************************************************************************/

		// Post para lluvia

	int agua = digitalRead(2);
	bool estadoLluviaSensor= 0;
  if (agua==LOW){
		//Serial.println("MOJADO..."); // SI ESTA LLOVIENDO
		estadoLluviaSensor= 1;
	} else{
		//Serial.println("SECO..."); //NO ESTA LLOVIENDO
	  estadoLluviaSensor= 0;
	}


	  JsonObject& lluvia = jsonBufferLluvia.createObject();
	  lluvia["state"] = estadoLluviaSensor; //Lo pone a 1 si esta lloviendo, si no a 0
		lluvia["date"] = 0;
	  lluvia["idDispositivo"] = 1;
	  char lluviaStr[100];
	  lluvia.printTo(lluviaStr);
		Serial.println("Se enviaran los datos de lluvia : \n");
		Serial.println(lluviaStr);
			Serial.println("\n");
		int	statusCodeLluvia = client.post("/restAPI/Proyecto/lluvia", lluviaStr, &responseLluvia);





/*****************************************************************************************
****************************************************HUMEDAD***************************************
******************************************************************************************/

	// Post para humedad
  JsonObject& humedad = jsonBufferHum.createObject();
  humedad["value"] = (float) humidity;
  humedad["date"] = 0;
  humedad["idDispositivo"] = 1;
  char humedadStr[100];
	humedad.printTo(humedadStr);
	Serial.println("Se enviaran los siguientes datos de humedad : \n");
	Serial.println(humedadStr);
		Serial.println("\n");
	int statusCodeHum = client.post("/restAPI/Proyecto/humedad", humedadStr, &responseHum);
	

/*******************************************************
********************************TOLDO****************
********************************************************/

// Post para toldo
JsonObject& toldo = jsonBufferToldo.createObject();

		// Estas condiciones del if son las que hay que modificar según queramos que se comporte el toldo. 
		// Estos valores han sido elegidos por nosotros, y con un análisis más profundo probablemente se obtendrán mejores
		if((float)temperature> 29 || 	estadoLluviaSensor== 1  || (float)humidity> 75 ){
				digitalWrite(16, HIGH);
				toldo["state"] = true;
		} else{
			digitalWrite(16, LOW);
				toldo["state"] = false;
}
toldo["date"] = 0;
toldo["idDispositivo"] = 1;
char toldoStr[100];
toldo.printTo(toldoStr);
Serial.println("Se enviaran los siguientes datos de toldo : \n");
Serial.println(toldoStr);
	Serial.println("\n");
int statusCodeToldo = client.post("/restAPI/Proyecto/toldo", toldoStr, &responseToldo);

delay(5000);


}
