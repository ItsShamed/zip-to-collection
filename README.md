# Beatmap pack archive to osu! collection

This Java program allow you to parse any beatmap pack and convert it into an [
.osdb osu! collection](https://gist.github.com/ItsShamed/c3c6c83903653d72d1f499d7059fe185).

## Usage

To run the program, you need at least Java 8 installed.

To use the user interface, run the jar file with no arguments.

You can also use it from the command line:

```shell
java -jar <whatever filename>.jar <input path> <output path>
```

## Known limitations

This program can't parse RAR5 archives due to laziness and dependency limitations. RAR4 has not been tested yet and can
be unstable.