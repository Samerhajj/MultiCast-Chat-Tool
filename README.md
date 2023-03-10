# MultiCast-Chat-Tool

The chat tool is a simple chat client 
![img.png](img.png)

The tool has an area to enter a multicast chat address and a port. Note that you must ensure that the
recipients are all listening on the same port on the multicast address. Otherwise they won’t be able to
communicate.
The tool includes a Join button to join the indicated multicast group via an IGMP message. It also includes
a Leave button to send an IGMP leave message. At the bottom is a Send button to send a new message
and a log at the bottom to show previous messages

![img_1.png](img_1.png)

The tool also includes a combobox to select the local interface to send the multicast messages out on.
In computers with multiple external network interfaces (ex. because the computer has an installation of
VirtualBox which creates a closed network environment on the computer), its necessary to select which local
interface will be used for external communications. The tool shown enumerates the network interface based
on their IPv4 addresses. An alternative would be to enumerate the network interfaces themselves by type
(ex. Ethernet, VirtualBox Host Only, Loopback, ISATAP interfaces).
