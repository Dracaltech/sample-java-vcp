# sample-java-vcp
Dracal // SDK code sample for Java on VCP

## Assumptions

Running this repository requires you to have installed:
- Java (version >= 11)
- Gradle (version >= 6.7)

You can test your setup by executing a build:
```
gradle build
```

## Simple usage

Make sure that in `Main.java`, variable `path` corresponds to your local setup (on Windows, your serial path might be e.g. `COM4`)

Run script
```
gradle execute
```

## Sample output


```
↑130 dracal/sample-java-vcp git:(main) ▶ gradle execute

> Task :execute
Awaiting info line...
[
I, Product ID, Serial Number, Message, MS5611 Pressure, Pa, SHT31 Temperature, C, SHT31 Relative Humidity, %]
2024/02/28 09:53:43 VCP-PTH450-CAL E24638
MS5611 Pressure           102590.0 Pa
SHT31 Temperature         22.8 C
SHT31 Relative Humidity   45.95 %


Poll interval set to 1000 ms
Printing 2 fractional digits
2024/02/28 09:53:43 VCP-PTH450-CAL E24638
MS5611 Pressure           102594.0 Pa
SHT31 Temperature         22.75 C
SHT31 Relative Humidity   45.94 %


2024/02/28 09:53:43 VCP-PTH450-CAL E24638
MS5611 Pressure           102594.0 Pa
SHT31 Temperature         22.75 C
SHT31 Relative Humidity   45.94 %


2024/02/28 09:53:44 VCP-PTH450-CAL E24638
MS5611 Pressure           102601.0 Pa
SHT31 Temperature         22.75 C
SHT31 Relative Humidity   45.94 %


<=========----> 75% EXECUTING [5s]
> :execute
^C%
↑130 dracal/sample-java-vcp git:(main) ▶
```
