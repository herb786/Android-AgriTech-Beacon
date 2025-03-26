# Una aplicacíon con balizas y Google Proximity Beacons para AgriTech

Las balizas *bluetooth* nos permiter trasmitir información no sólo de proximidad entre objectos sino informaci´øn más detallada si hacemos uso de los **servicios GATT**.
Veámos ahora como podemos sondear las eras de un fundo usando estás balizas y Proximity Beacons, una de las API de Gooogle.

La interfaz de servicio Proximity Beacons permite asignar cabeceras o mensajes a cada baliza. Estas balizas son identifcados con su identifcador único **UUID**. Estos mensajes se encuentran en la nube y pueden ser editados sin problemas para tener mensajes apropiados en cada estación y lugar. Por tanto no necesitamos borrar y reprogramar las balizas para incluir nuevos mensajes pero necesitamos cambiar los datos adjuntos usando la API de Google.

Una vez preparados los mensajes a distribuir, usaremos la API Nearby Messages para transferir este información al dispositivo móvil.

## Registrar baliza con Beacon Proximity
Antes de crear estos mensaejes debemos tener a mano nuestros respectivos credenciales con protocolo OAuth y generar llaves de autorización.

Primero creamos una identificación para nuestra baliza. En este caso necesitamos los parámateros **proximity UUID**, *major* y *minor*. 

El valor **minor** es usado para obtener una ubicación más precisa de la baliza. Por ejemplo podemos indicar el número de pabellón, departamento, oficina, plataforma, sección, etc.

El valor **major** es menos preciso. Por ejemplo podemos indicar si estamos en un emplazamiento, edificio, instalación, etc.

La identificación de una baliza es el resultado de <mark>concatenar los valores de proximity uuid, major, minor</mark> y luego cifrarlo usando *base64*. La siguiente orden servirá para crear la identificación.
```bash
echo -ne $hexuid | xxd -r -p | base64
```
Aquí **$hexuid** es la concatenación de os valores indicados anteriormente. Luego podemos registrar la baliza en el servicio Beacon Proximity API. Esta consulta con curl registrará nuestra baliza

```bash
curl -d '{"advertisedId":{"type":"IBEACON","id":"'$uid'"},"status":"ACTIVE","placeId":"ChIJTxax6NoSkFQRWPvFXI1LypQ","latLng":{"latitude":"47.6693771","longitude":"-122.1966037"},"indoorLevel":{"name":"1"},"expectedStability":"STABLE","description":"Farm beacon.","properties":{"position":"entryway"}}' -H 'Content-Type: application/json' -H "Authorization: Bearer $token" https://proximitybeacon.googleapis.com/v1beta1/beacons:register
```
Aquí **$uid** is la identificación generada con base64 y **$token** es la llave de autorización para usar la API de google.

## Mensaje localizados con Beacon Proximity
Luego de registrar la baliza, podemos adjuntar mensajes conocidos como **attachments**. Estos mensjaes puden ser creados en la consola web de la nube de Google o por medio de consulta HTTP de este mdoo
```bash
curl -d '{"namespacedType":"'$namespaceName'/string","data":"UmlwZSBqdWljeSBwbHVtcw=="}' -H "Authorization: Bearer $token" -H 'Content-Type: application/json' https://proximitybeacon.googleapis.com/v1beta1/$beaconName/attachments?key=$apikey
```
Necesitamos el valor de **$namespaceName** que es el nombre asignado al proyecto, **beaconName** que es el nombre de la baliza que se creó luego del registro y la <mark>notificación cifrada en base64</mark> para la llave **data** del *payload*.

En nuestro caso ciframos el mensaje "Ripe juicy plums" para indicar que hay ciruelas maduras en una parcela del fundo. 
```bash
echo -n "Ripe juicy plums" | base64
```

## Notificaciones con Nearby Messages API
Usaremos las notificaciones para obtener información del estado de las diferentes parcelas colocando balizas en cada parcela del fundo.

Ahora usemos los datos servidos por Nearby Message API para mostrar estas notificaciones. 

Primero debemos crea una llave de autorización para que los dispositivos móviles puedan usar esta API y luego modicar el **manifest** del aplicativo.


Luego de generar la llave de autorización debemos agregar la dependencia Nearby Message a nuestro proyecto del modo siguiente
```java
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
...
<uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false" />
...
<application>
    <meta-data
            android:name="com.google.android.nearby.messages.API_KEY"
            android:value="your_authorization_key" />
</application>
```
Agregamos la siguien dependencia al fichero **gradle**

`implementation 'com.google.android.gms:play-services-nearby:15.0.1'`

Esta pedazo de código se usará para suscribirse al servicio *Nearby Message* para recibir las notificaciones

```java
...
public final String IBEACON = "fda50693-a4e2-4fb1-afcf-c6eb07647825";
...
MessageListener mMessageListener = new MessageListener() {
    @Override
    public void onFound(Message message) {
        Log.println(Log.ASSERT, TAG, "Message found: " + message);
        imgNotification.setImageResource(R.drawable.plums1);
        if (!message.getNamespace().equals(NAMESPACE)){
            new ProximityAsyncTask().execute();
        } else{
            txtNotification.setText(new String(message.getContent()));
        }
    }

    @Override
    public void onLost(Message message) {
        Log.println(Log.ASSERT, TAG, "Lost sight of message.");
        imgNotification.setImageResource(R.drawable.farm);
        txtNotification.setText("Welcome to my Farm! ");
    }

};
...
@Override
public void onStart() {
    super.onStart();
    MessageFilter messageFilter = new MessageFilter.Builder()
            .includeIBeaconIds(UUID.fromString(IBEACON), null, null)
            .includeAllMyTypes()
            .build();
    SubscribeOptions options = new SubscribeOptions.Builder()
            .setStrategy(Strategy.DEFAULT)
            .setFilter(messageFilter)
            .build();
    Nearby.getMessagesClient(this).subscribe(mMessageListener, options);
}
...
@Override
public void onStop() {
    Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
    super.onStop();
}
...
```

![alt text](https://s3-us-west-2.amazonaws.com/py4hacaller/device-2018-08-25.gif)

### Referencias
1. https://developers.google.com/beacons/proximity/guides
2. https://developers.google.com/beacons/proximity/reference/rest/v1beta1/beacons.attachments/list
3. https://developers.google.com/beacons/dashboard/
4. https://developers.google.com/nearby/messages/android/get-started
5. https://en.wikipedia.org/wiki/IBeacon
6. https://github.com/google/eddystone/tree/master/eddystone-uid