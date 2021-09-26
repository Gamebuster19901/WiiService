package com.gamebuster19901.wiiservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

import com.gamebuster19901.wiiservice.ParamString.Pair;

public class WiiService {
	
	private final String domain;
	
	/**
	 * Creates a WiiService to obtain data from the specified altWFC
	 * 
	 * @param domain the domain name of the altWFC
	 */
	public WiiService(String domain) {
		this.domain = domain;
	}
	
	/**
	 * <p>Uses the {@link ParamString} protocol to obtain uniqueNicks of a PID's friends.
	 * You must know the PID of the questioner's friends.
	 * 
	 * <p>Returns a sequence of ParamString pairs that represents the entire server response.
	 * 
	 * <p>Original Documentation:
	 * 
	 * <p>The client sends a list with profile ids (TCP port 29901) to the server to translate them into user nicks.
	 * Example:
	 * 
	 * <pre>
	 *       otherslist =
	 *          sesskey = 210997796
	 *        profileid = 302594991
	 *         numopids = 3
	 *            opids = 469604577|447214276|354860031
	 *      namespaceid = 16
	 *         gamename = mariokartwii
	 *            final /
	 * </pre>
	 * The parameters sesskey and numopids are ignored by the server.
	 * 
	 * <p>The server answers with a sequence of o+uniquenick pairs; one pair for each requested id of opids.
	 * <p>The usage of the nick names is not clear yet. The list is sorted by the profile ids of the parameter o.
	 * Parameter oldone terminates the sequence.
	 * 
	 * <pre>
	 *       otherslist =
	 *                o = 354860031
	 *       uniquenick = 4anbjhi1jRMCJ23ioucc
	 *                o = 447214276
	 *       uniquenick = 7dkt0p6gtRMCJ2ljh72h
	 *                o = 469604577
	 *       uniquenick = 7hl05oif6RMCJ142q65e
	 *           oldone =
	 *            final /
     * </pre>
	 * 
	 * @see ParamString ParamString for more information about the ParamString protocol
	 * 
	 * @param gameName
	 * @param questionerID the PID of the 
	 * @param profileIDs the profile ids to get the nicknames of
	 * @return a sequence of ParamString pairs that represents the entire server response.
	 * @throws UnknownHostException if the IP address of the domain cannot be determined
	 * @throws IOException if an IOException occurs
	 */
	public ArrayList<Pair<String, String>> getNicknames(String gameName, int questionerID, int... profileIDs) throws UnknownHostException, IOException {
		
		String searchString = Arrays.toString(profileIDs).replace("[", "").replace(",", "|").replace("]", "").replace(" ", "");
		ParamString transmission = new ParamString();
		transmission.add("otherslist");
		transmission.add("sesskey", 210997796);
		transmission.add("profileid", questionerID);
		transmission.add("numopids", profileIDs.length);
		transmission.add("opids", searchString);
		transmission.add("namespaceid", 16);
		transmission.add("gamename", gameName);
		transmission.finall();
		
		Socket socket = new Socket("gpsp.gs." + domain, 29901);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream response = new DataInputStream(socket.getInputStream());
		
		out.write(transmission.toString().getBytes());
		out.flush();
			
		String data = "";
		boolean finished = false;
		while(!finished) {
			while(response.available() == 0) {}
			data = data + (char)response.read();
			System.out.println(data);
			if(data.contains("\\final\\")) {
				finished = true;
			}
		}
		response.close();
		socket.close();
		
		return ParamString.fromString(data).toPairs();
	}
	
	/**
	 * <p>Gets the game availability. Despite documentation to the contrary, this appears to always be
	 * `fe fd 09 00 00 00 00` for any ganeName in AltWFCs, even completely fabricated gameNames.
	 * 
	 * <p>Original documentation:
	 * <p>
	 * <p>First the Wii checks if the server is available (UDP port 27900)
	 * <pre>
	 * 09 			is the record type: (AVAILABLE)
	 * 00 00 00 00		status ("disabled services" bit field)
	 * 6d 61 72 69 6f 6b \
	 * 61 72 74 77 69 69	gamename ("mariokartwii in this case")
	 * 00 </pre>
	 * 
	 * <p>If it is available, the bitfield is empty (all services are available):
	 * 
	 * <pre>
	 * fe fd 09 00 00 00 00
	 * </pre>
	 * <pre>
	 * fe fd		Nintendo-Answer
	 * 09		Type (AVAILABLE)
	 * 00 00 00 00	status </pre>
	 * 
	 * <p>When the server is down permanently, bit 1 is set:
	 * 
	 * <pre>
	 * fe fd 09 00 00 00 01
	 * </pre>
	 * 
	 * <pre>
	 * fe fd		Nintendo-Answer
	 * 09		Type (AVAILABLE)
	 * 00 00 00 01	status
	 * </pre>
	 * 
	 * <p>(Results in "20110").
	 * <p>
	 * <p>When the server is having maintenance (temporarily down), bit 2 is set:
	 * 
	 * <pre>
	 * fe fd 09 00 00 00 02
	 * </pre>
	 * <pre>
	 * fe fd		Nintendo-Answer
	 * 09		Type (AVAILABLE)
	 * 00 00 00 02	status
	 * </pre>
	 * 
	 * <p>(Results in "20101").
	 * 
	 * @param gameName
	 * @return the hex string representation (with no spaces) of the availability bitfield
	 * @throws UnknownHostException if the IP address of the domain cannot be determined
	 * @throws IOException if an IOException occurs
	 */
	@Deprecated
	public String getGameAvailability(String gameName) throws UnknownHostException, IOException {
		byte[] transmissionStart = new byte[] {0x09, 0x00, 0x00, 0x00, 0x00};
		byte[] transmissionMid = gameName.getBytes();
		byte[] transmissionEnd = new byte[] {0x00};
		
		byte[] transmission = new byte[transmissionStart.length + transmissionMid.length + transmissionEnd.length];
		System.arraycopy(transmissionEnd, 0, transmission, 0, transmissionEnd.length);
		System.arraycopy(transmissionMid, 0, transmission, 0, transmissionMid.length);
		System.arraycopy(transmissionStart, 0, transmission, 0, transmissionStart.length);
		
		InetAddress availabilityServer = InetAddress.getByName(gameName + ".available.gs." + domain);
		
		DatagramPacket transmissionPacket = new DatagramPacket(transmission, 0, transmission.length, availabilityServer, 27900);
		DatagramSocket socket = new DatagramSocket(27900);
		socket.send(transmissionPacket);
		
		byte[] receiveData = new byte[7];
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		socket.receive(receivePacket);
		socket.close();
		return DatatypeConverter.printHexBinary(receivePacket.getData());
	}
	
	/**
	 * There exists a large set of domains following the name scheme {@code <GAME>.ms<NUMBER>.gs.nintendowifi.net}.
	 * All domains point to the same server. Mario Kart Wii uses mariokartwii.ms19.gs.nintendowifi.net.
	 * 
	 * The server listens at TCP port 28910 and accepts database queries to handle matchmaking globally.
	 * The server is also used in private rooms to check if a profile is already connected to WFC.
	 * 
	 * @param gameName
	 * @return the full game server URL
	 */
	public String getMasterDatabaseServerGameURL(String gameName) {
		gameName = gameName.toLowerCase();
		int server = 0;
		for(int i = 0; i < gameName.length(); i++) {
			char c = gameName.charAt(i);
			server = c - (server * 0x63306ce7);
		}
		server %= 20;
		
		return gameName + ".ms" + server + "." + domain;
	}
	
}
