/**
 * This package contains code relating to
 * <a href="http://wiki.vg/Plugin_channels/World_downloader">WDL's plugin
 * channel API</a> and the in-mod permission system.
 * 
 * This is <em>intended</em> to give server owners degrees of control
 * as to use of the mod, rather than forcing them to kick players based
 * off of forge/liteloader handshakes.  Not all servers are using it
 * this way, but those that use it correctly generally have a cleaner
 * user experience.  This mechanism also allows for permission requests.
 * 
 * Note that due to the way that <a href="http://wiki.vg/Plugin_channels">
 * plugin channels</a> work, a REGISTER packet must be sent to the server
 * before the system can be used.  Aditional packets are sent to help
 * manage the system.
 */
package wdl.chan;