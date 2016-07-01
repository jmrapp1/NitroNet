Installing NitroNet
=================
There are 2 ways to "install" NitroNet into your Java application. You can add the entire raw source code to your project or you can include the NitroNet jar file in your project's build path. I will be explaining how you can install it using the jar file and eclipse. 

###Download The Jar File
1. You are going to first want to go to the location of the jar file [here](https://github.com/baseball435/NitroNet/blob/master/final%20jar/NitroNet.jar).
2. Click on the "Raw" button to download the jar file to your file system.

###Including The Jar File In Your Project
1. Create a standard Java project in eclipse and create a new folder within it called "libs". (You can do this by right clicking the project and going to New->Folder)
2. Then you will want to take the NitroNet jar file and copy it into that folder. If you did this through Eclipse it should show up. If you copied it through the standard file system you will need to click on the project name and press "F5" to refresh the project.

###Including NitroNet In The Build Path
1. Right click on the project and go to "Properties".
2. One of the tabs on the left will say "Java Build Path". Select it.
3. You will now see 4 tabs: "Source", "Projects", "Libraries", "Order and Export". Select the "Libraries" tab.
4. Click the button that says "Add JARs". Make sure you do NOT select "Add External JARs".
5. Navigate to your project->"libs" folder->NitroNet.jar
6. It should now appear in the list of jars. 

###Done!
You should now be able to fully use NitroNet in your project!
