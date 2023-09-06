# GUILib
 A lightweight spigot library to create GUIs

## Installation
### Maven
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.4</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>io.github.sebazcrc.guilib</pattern>
                        <!-- Replace 'com.yourpackage' with your package ! -->
                        <shadedPattern>com.yourpackage.guilib</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>
```
Repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
	<url>https://jitpack.io</url>
    </repository>
</repositories>
```

Dependency
```xml
<dependency>
    <groupId>com.github.SebazCRC</groupId>
    <artifactId>GUILib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Hook
You have to initialize the GUICore in your plugins "onEnable" method

```java
public void onEnable() {
    GUILibCore.init(this);
}
```

## Creating a GUI
You can choose between using a PagedGUI or a SinglePagedGUI.
The paged GUI can technically be used a single paged as well, is up to you to choose which one you want to use.

### Understanding the GUI
Both of them have a constructor that request a Player as the first parameter, parsing a Player will cause the library to make the GUI exclusive to that player.
This means that no one else but the player will be able to interact with the GUI.
I would recommend you to use this feature only if you want to assign a single GUI instance for each player instead of a GUI instace for everyone.
In case you want to create a single GUI that can be clicked by anyone, do not use this constructor or send a null player.

### Pages
In case you are using a PagedGUI, you might want to take into account that you will most likely have to send the library what page you want to perform the changes in.
Every created page has an index (which starts from 0)
If you use a SinglePagedGUI, you do not have to deal with this.

### Creating a PagedGUI
First, we need to create the itemstacks that will represent a previous or next page icon.
```java
// Creates the itemstack that represents the "previous page" button
ItemStack previous = new ItemStack(Material.ARROW);
ItemMeta mp = previous.getItemMeta();
mp.setDisplayName(ChatColor.GREEN + "Previous page");
previous.setItemMeta(mp);

// Creates the itemstack that represents the "next page" button
ItemStack next = new ItemStack(Material.ARROW);
ItemMeta np = next.getItemMeta();
np.setDisplayName(ChatColor.GREEN + "Next page");
next.setItemMeta(np);
```

We can create a PagedGUI instance the following way.
* This will create a player-exclusive GUI as explained above.
* Will set the title to "&aMy Inventory"
* It will have 5 rows (9*5 slots)
* The previously created ItemStacks will be used as previous / next buttons.

```java
// Creates a new PagedGUI, a GUI that will allow more than one page to be used.
// Since we passed a player as a parameter, this will cause the library to make this GUI exclusive for this player.
// If you would like to create a single GUI to be accessed by anyone, do not pass a player as a parameter.
PagedGUI gui = new PagedGUI(e.getPlayer(), ChatColor.GREEN + "My inventory", 5, previous, next);

// This method must be called every time you create a GUI
gui.createInventory();
```

Some of the methods that you can use are the following
We can use a consumer to listen to InventoryClickEvent without registering any listener

```java
// Sets an item on the slot 10, whenever a player clicks on it, they receive a message
gui.setItem(10, new ItemStack(Material.APPLE), (click -> {
    Player player = (Player) click.getWhoClicked();
    player.sendMessage("Yay!!! Apples!!!");
}));

// Sets an item on the slot 11, whenever a player clicks on it, they receive a message
gui.setItem(11, new ItemStack(Material.DIAMOND), (click -> {
    Player player = (Player) click.getWhoClicked();
    player.sendMessage("diamond");
}));

// Adds an item in the first available slot
gui.addItem(new ItemStack(Material.OBSIDIAN), (click -> {
    Player player = (Player) click.getWhoClicked();
    player.sendMessage("added item");
}));

// Fills the empty slots with the given type
gui.fillWith(Material.GRAY_STAINED_GLASS_PANE);

// Adds a new page
gui.addPage();

// Sets an item in the page index 1 (second page), whenever someone clicks their inventory is closed
gui.setItem(1, 35, new ItemStack(Material.BEDROCK), (event -> {
    event.getWhoClicked().closeInventory();
}));

// Opens the GUI for the player
gui.open(e.getPlayer());
```

