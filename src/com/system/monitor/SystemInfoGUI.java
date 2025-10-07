package com.system.monitor;

import java.awt.*;
import javax.swing.*;

import com.systeminfo.SystemInfo;

public class SystemInfoGUI {
	public static void main(String args[])
	{
		JFrame frame = new JFrame("Sky Watch");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("MonoSpaced", Font.PLAIN, 14));
		
		String sysInfo = SystemInfo.getAllSystemInfo();
        textArea.setText(sysInfo);

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER); 
        
        frame.setVisible(true);
	}
}
