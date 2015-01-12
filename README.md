neoPaintingSwitch 1.36
=====================

This plugin uses the same basic idea as located [here](http://forums.bukkit.org/threads/inactive-edit-paintingswitch-v0-1-choose-paintings-with-ease-672.5788/) and [here] (http://forums.bukkit.org/threads/edit-paintingswitch-v0-4-scroll-through-paintings-1185.24604/).

This plugin was originally created by [Edward Hand](http://forums.bukkit.org/members/edward-hand.13332/) (credit to him!) and also eventually updated by [MadMonkeyCo](http://forums.bukkit.org/members/madmonkeyco.22820/). However is was abandoned again and because I use it I needed it working. Basically, it simplifies the selecting of paintings and saves alot of time. Permissions are supported.

Features:
=========
* Allows you to scroll through paintings by aiming and right clicking at them and using the mouse wheel to scroll through paintings.
* Plugin will remember what painting was last used and will attempt to use that painting again for future placed paintings
* Permissions support "neopaintingswitch.use".
* Supports worldguard

Version 1.36
============
* Updated for use with Worldguard 6
* Implemented the missing debug config option for permissions debugging

Version 1.35
* added bPermissions and Vault permissions support
* updated event priorities to be friendlier to other plugins
* small code changes to internal player data storage
* other misc changes
* Removed deprecated method calls
* Ops overrides permissions

Version 1.34
* Plugin now remembers what painting was last used and attempts to use it for future placed paintings

Version 1.33
* Updated for MC1.4 and replaced deprecated methods
* Incorporated WG pull request from BangL

Version 1.321:
* Added worldguard support for members of a region not just owners.
* Fixed config bug

Version 1.32:
* Added worldguard support
* Fixed painting dupping bug

Version 1.31:
* updated for bukkit 1.1 and mc 1.1 new event handlers
* also respect for outside event cancel events

Version 1.3:
* PermissionsEx support & new config option to turn off permissions and allow Free For All mode.

Version 1.2:
* Significantly reduced the number of calls to getTargetBlock(). Now only calls when precise conditions are met and only once.

Version 1.1
* May have fixed a exception

Version 1.0
* First release.
