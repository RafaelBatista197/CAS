# CAS
This project is an prototype application developed (contexto escolar?) which consists of a mobile application in Android, called “Stay Active”, with 
the objective of fighting this bad habit and sedentarism of people. The application is able to detect both context and cognition and works as 
follows: after a certain period of time (chosen by the liking of the user), using Google Awareness API and fences, the application detects the
user has been still for too long and sends him a notification, recommending taking a break and do some simple exercises. The exercises
can be tracked by opening the camera in the application, which will then, through machine learning, detects the poses and counts the repetitions. 


Steps to run the application:

1 - For this application to work you must have a device with Android 11 at least.

2 - Clone the project.

3 - The application needs a API key in order to the Awareness API used on this application work. To get this API, you need to create an account on Google
Cloud Platform (GCP) and the register the Awareness API, which will return the API key (more informations here: https://cloud.google.com/apis/docs/getting-started). 
After registering the API key on the platform you must go to the AndroidManifest.xml file and put the key in the following parameter:

    <meta-data
         android:name="com.google.android.awareness.API_KEY"
         android:value="YOUR_API_KEY"/>
         
 where "YOUR_API_KEY" is where you put the key you got from the platform.
 
 4 - Run the project and it shoudld work!.
