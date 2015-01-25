<p><b>Individual Assignment 2</b>
</p><p>Project was written in Android Studio using the supplied Navigation Drawer template. The apk and source code are under the Recognize directory in the attached archive.
</p><p>On opening the app, the user sees the canvas. The user can draw on the canvas using multiple colors chosen by either hitting the paint palette or paint color button, clear and undo using the appropriate buttons, and save the strokes as an object (consisting of an ArrayList of Stroke objects as well as any recognized text) to internal storage using the save button. The user can also recognize text, which sends the data to the cloud recognition site in an AsyncTask, using the recognize text action bar button, as well as share the current drawing as a jpeg using the share button, which triggers and implicit intent of sharing a jpeg image. This also saves the jpeg in the user's photos directory on external storage.
</p><p>By accessing the Navigation Drawer the user can open the second activity (for a result, the drawing to load), which shows any saved drawings in a ListView supplied by an adapter that displays a small canvas as an icon as well as any recognized text. The user can either select a drawing to open, or long press on an item to open up a contextual action bar which allows for selecting multiple drawings to delete, as well as deleting all the drawings. On loading a drawing, the result is passed back to the main canvas view, which now shows the loaded drawing as well as any recognized text.
</p><p>Project was tested on a device and emulator running Android 4.4.2. Application requires Android 4.0 (API 14) or higher because of the MultiChoiceModeListener used to select multiple saved drawings in the saved drawings activity.
</p><p>Icons were obtained from <a href="http://www.clker.com/">[1]</a>.
</p><p><b>Challenges and Lessons Learned</b>
</p><p>This project helped me learn a lot about some specifics of Android in particular, as well as some more Java in general. On the Android side, it was interesting to learn about how to register user input and use it to draw on the canvas by supplying the canvas with a custom Line or Stroke object array and telling it to invalidate and call the onDraw method. It was also a challenge to learn how to appropriately call and pass data to the recognition service using an AsyncTask off of the main UI thread, as well as get that data back. I got to learn about passing data between activities using intents and starting activities for results as well. I learned about how to use the Android contextual action bar with a multi-choice mode listener, as well as using adapters to supply and alter the underlying data of a ListView and how to customize and extend adapters to display the data how I wanted to. I also got to use Android's implicit intent sharing feature to share jpeg images created from drawing objects, and how to save these data types to both internal and external storage in Android.
</p><p>This also helped me learn more about aspects of Java, especially implementing the Serializable interface on custom objects to be able to save them and load them using Object streams. This project was fun to do and really helped me learn a lot.
</p><p><b>Extra Credit</b>
</p><p>1. User can save, load, and multi-select/delete ink traces as objects, as well as save and share jpeg versions of their drawings.
</p><p>2. The widget containing the canvas used for ink capture and display is a customized widget extending the Android View class, DrawWidget.java.
</p><p><b>Screenshots</b>
</p>
<img src="http://i.imgur.com/q6GbgJa.png"><br><br>
<img src="http://i.imgur.com/kkWMyaS.png"><br><br>
<img src="http://i.imgur.com/4V0Oi5Z.png"><br><br>
<img src="http://i.imgur.com/vZGk4ft.png"><br><br>
<img src="http://i.imgur.com/AuL1aes.png"><br><br>
<img src="http://i.imgur.com/kSX1UlJ.png"><br><br>
<img src="http://i.imgur.com/FLDa6zv.png"><br><br>
<img src="http://i.imgur.com/1lcj5J6.png"><br><br>
<img src="http://i.imgur.com/ukHneoO.png"><br><br>
<img src="http://i.imgur.com/TnzTTNc.png"><br><br>
<img src="http://i.imgur.com/x7r8CJr.png"><br><br>
