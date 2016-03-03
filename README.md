#eclipse-crops
## The maven version

### A little bit about the structure of the filesystem
This version begins to divide the functionality into separate folders, with Eclipse projects under each. 
With this first commit, the "releng" folder (which stands for Release Engineering) houses the relatively 
empty org.yocto.crops plugin project and it's corresponding org.yocto.crops-feature project, 
the org.yocto.crops.target project can be thought of as the manifest for the development of the plugin
and finally the org.yocto.crops.repo project exists to help package and deploy the plugin to a "p2" update site.
 
Maven uses heirarchical POM files to build. The highest level pom.xml file is known as the parent-pom by its children.
If a given project (module in the parent pom) has sub-children, they would each have their own pom.xml as well.

### How to setup your development environment (to develop the plugin itself)
1. Download and launch the eclipse-installer
2. Click on the "menu" icon in the upper righthand corner and change to Advanced mode.
3. Under "Eclipse.org" choose "Eclipse IDE for Eclipse Commiters", as this will include many of the needed tools
4. Click next
5. Select "Github projects".
6. Click the "plus" icon to add a project file.
7. Enter "http://github.com/crops:8888/todor/eclipse-crops/blob/timo/eclipse-crops-maven/releng/CROPS.setup" into the Resource URI box.
8. A new "CROPS" project should now be added under ```<User>```. Click on it and install.
9. The Ooomp installer will now download and install all the required plugins and jar files and create an eclipse instance to run it.
10. If you deselected "Bundle Pools", you would also get your own "p2" repository in the newly created environment. This allows you to develop in more than one version of Eclipse.

Once the installer has finished downloading, it should launch the new Eclipse environment for you. If we had it all figured out already, it would also have downloaded the ```eclipse-crops``` plugin source and imported the projects into your workspace. We don't have it all figured out already.

### How to import the projects into your workspace (the semi-automatic Egit way)
1. Click on Workbench in the start up splash screen.
2. In the Perspective menu, choose Git.
3. In the Git Explorer view, choose "Clone a git respository and add it to this view"
4. <Do the thing that makes Egit import the projects for you>
5. In the Perspective menu, choose Plug-in Development.

### How to build the projects using m2e/Tycho inside Eclipse

### How to build the projects using maven on the command-line 