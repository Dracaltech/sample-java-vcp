package src.main.java;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Main {
  // replace with appropriate path, interval, and baudRate
  static String path = "/dev/ttyACM0";
  static int interval = 1000;
  static int baudRate = 9600;

  // working globals
  static String[] info_line;
  static int padlen;

  public static void main(String[] args) {
    // open port
    SerialPort serialPort = new SerialPort(path);
    try {
      serialPort.openPort();
      serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    } catch (SerialPortException ex) {
      System.out.println("Error setting up serial port: " + ex);
    }

    // write instructions
    try {
      serialPort.writeBytes("INFO\r\n".getBytes());
      Thread.sleep(1000);
      serialPort.writeBytes("POLL 1000\r\n".getBytes());
      Thread.sleep(1000);
      serialPort.writeBytes("FRAC 2\r\n".getBytes());
      Thread.sleep(1000);
    } catch (SerialPortException | InterruptedException ex) {
      System.out.println("Error writing to serial port: " + ex);
    }

    // read event
    try {
      serialPort.addEventListener(new SerialPortEventListener() {
        StringBuilder line = new StringBuilder();

        @Override
        public void serialEvent(SerialPortEvent event) {
          if (event.isRXCHAR()) {
            try {
              // accumulate reads until the end of line
              byte[] buffer = serialPort.readBytes();
              for (byte currentByte: buffer) {
                if ( (currentByte == '\r' || currentByte == '\n') && line.length() > 0) {
                  // send for processing
                  processData(line.toString());

                  line.setLength(0);
                }
                else {
                  line.append((char) currentByte);
                }
              }
            } catch (SerialPortException ex) {
              System.out.println("Error reading from serial port: " + ex);
            }
          }
        }
      });
    } catch (SerialPortException ex) {
      System.out.println("Error setting up event listener: " + ex);
    }
  }

  // parse a data line and display results
  private static void processData(String data) {
    String[] split = data.replace(", ", ",").split("\\*")[0].split(",");

    // extract info line
    if (split[0].contains("I")) {
      // parse field titles
      if (split[1].equals("Product ID")) {
        info_line = split;

        List<String> values = Arrays.asList(Arrays.copyOfRange(split, 4, split.length));
        padlen = values.stream().map(String::length).max(Integer::compareTo).get();

        System.out.println(Arrays.toString(info_line));
      } else {
        System.out.println(split[3]);
      }
      return;
    }
    if (info_line == null) {
      System.out.println("Awaiting info line...");
      return;
    }

    // parse readout values
    String device = split[1] + " " + split[2];
    String[] sensors = new String[(info_line.length - 4) / 2];
    double[] values = new double[sensors.length];
    String[] units = new String[sensors.length];

    String[] info = Arrays.copyOfRange(info_line, 4, info_line.length);
    for (int i = 0; i < info.length - 1; i += 2) {
      sensors[i/2] = info[i].trim();
      values[i/2] = Double.parseDouble(split[i + 4]);
      units[i/2] = split[i + 5].trim();
    }

    // print result
    String now = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
    System.out.println(now + " " + device);
    for (int i = 0; i < units.length; i++) {
      System.out.println(String.format("%-" + (padlen + 2) + "s %s %s", sensors[i], values[i], units[i]));
    }
    System.out.println("\n");
  }
}