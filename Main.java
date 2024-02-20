import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Main {
  public static void main(String[] args) {
    // replace with appropriate path, interval, and baudRate
    String path = "/dev/ttyACM0";
    int interval = 1000;
    int baudRate = 9600;

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
      Thread.sleep(100);
      serialPort.writeBytes("POLL 1000\r\n".getBytes());
      Thread.sleep(100);
      serialPort.writeBytes("FRAC 2\r\n".getBytes());
      Thread.sleep(100);
    } catch (SerialPortException | InterruptedException ex) {
      System.out.println("Error writing to serial port: " + ex);
    }

    // read event
    try {
      serialPort.addEventListener(new SerialPortEventListener() {
        String info_line;
        int padlen;

        @Override
        public void serialEvent(SerialPortEvent event) {
          if (event.isRXCHAR()) {
            try {
              byte[] buffer = serialPort.readBytes();
              if (buffer != null) {
                String data = new String(buffer);
                String[] split = data.replace(", ", ",").split(",");

                // extract info line
                if (split[0].equals("I")) {
                  // parse field titles
                  if (split[1].equals("Product ID")) {
                    info_line = data;
                    String[] infoSplit = info_line.split(",");
                    String[] values = new String[infoSplit.length - 4];
                    for (int i = 4; i < infoSplit.length; i++) {
                      values[i - 4] = infoSplit[i];
                    }
                    double[] numberValues = new double[values.length / 2];
                    for (int i = 0; i < values.length; i += 2) {
                      numberValues[i / 2] = Double.parseDouble(values[i]);
                    }
                    padlen = 0;
                    for (String s : values) {
                      if (s.length() > padlen) {
                        padlen = s.length();
                      }
                    }
                    System.out.println(info_line);
                  } else {
                    System.out.println(split[3]);
                  }
                  return;
                }
                if (info_line == null) {
                  System.out.println("Awaiting info line...");
                  return;
                }

                // extract readout values
                String device = split[1] + " " + split[2];
                String[] sensors = info_line.split(",");
                double[] values = new double[sensors.length / 2];
                String[] units = new String[sensors.length / 2];
                for (int i = 4; i < sensors.length; i += 2) {
                  sensors[i] = sensors[i].trim();
                  values[i / 2] = Double.parseDouble(split[i]);
                  units[i / 2] = split[i + 1].trim();
                }

                // print result
                System.out.println(new java.util.Date().toString() + " " + device);
                for (int i = 0; i < units.length; i++) {
                  System.out.println(String.format("%-" + (padlen + 2) + "s %s %s", sensors[i], values[i], units[i]));
                }
                System.out.println("\n");
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
}