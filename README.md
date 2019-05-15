# dn Tables

Some minor code to generate dN random tables in PDF format for various OSR RPG use.

LaTeX or direct PDF output is supported. There is a lot more flexibility with your final form if you use LaTeX, since this tool simply dumps your tables into a container document which you must then compile with LaTeX if you want a PDF.

## Command Line Usage

```
Usage: program-name [options] input-file output-file

Options:
  -t, --latex  LaTeX output file mode
  -p, --pdf    PDF output file mode
  -j, --json   JSON output file mode
  -e, --edn    END output file mode
  -h, --help
```

Examples:

Generate an EDN file from the family weapons source text.

```
java -jar dntables-0.1.0.jar -e goatmansgoblet/familyweapons.txt fw.edn
```

Generate a TEX file from the dw d6 democracy source text.

```
java -jar dntables-0.1.0.jar -t ~/projects/dntables/resources/dwdiscord/d6d.txt d6d.tex
```

## Compiling LaTeX

```
xelatex output.tex && open output.pdf

```

## Example Output

[Goatman's Goblet Family Ties Weapons PDF](/samples/family-weapons-tables.pdf)

![(d12) d4 Damage Weapons](/samples/d12example.png?raw=true "d12 table example")

[DW Discord D6D](/samples/d6d.pdf)

## Fonts

TL;DR, the [DCC](http://goodman-games.com/dungeon-crawl-classics-rpg/) fonts:

[Duvall](https://www.dafont.com/duvall.font)

[Book Antiqua](https://www.wfonts.com/font/book-antiqua)

[IM Fell English Pro](https://www.fontsquirrel.com/fonts/im-fell-english-pro)

## Input Text Format

Source is a plain text file with one or more numbered random tables.
Copying and pasting straight out of a discord channel works fine usually, depending on how the participants of the table generation entered their text. At the top of the file, you can add metadata as described below if you wish to have this in your final output.

### Table Titles

The following examples will trigger table capture:

```
[1d20] Gifts for a goblin king
What happened in the alley behind the stables (2d6)
(d66) Magical potions with temporal effects
```

The thing to note is that either at the start, or end of the line of the prompt, you must encapsulate the die type in parenthesis or square brackets.

### Prompt Entries

Prompt entries must start with a number and be followed by a space or other delimiter. Examples:

```
1. This is a valid entry for a prompt, d30 blah blah modifications.
1a. This is also valid.
1c. a..b..c labels are just considered next entries in the total entry count.
4 So Is this
8: The numeric labels are actually ignored, which may or may not be desireable.
```

see `dntables.parsers.text/parse-entry` for the exact regular expression if you really want to know ALL current entry marker possibilities.

## Intermediate EDN/JSON Spec

## Attachable Metadata

Within the source text the following attributes can be added. Place them on a line by themselves.
They may not span multiple lines.

- ::license [text]
- ::licenseurl [text]
- ::author [text]
- ::source [text]
- ::doctitle [text]
- ::fontsize [+1, -1, 1] - Sets the font size for the next table encountered to either be relative or absolute sized. Currently only works with direct PDF output.


## Inspiration

- [DCC RPG](http://goodman-games.com/dungeon-crawl-classics-rpg/)
- [Dungeon World](https://dungeon-world.com/)
- [Weapons for Family Ties Table](http://www.goatmansgoblet.com/2019/04/ose-weapons-for-family-ties-by-damage.html)
- Dungeon World Discord
- RPGTalk Discord
- Phishcord
- King Gizzard & The Lizard Wizard
- Phish

## License

Copyright © 2019 Damion Junk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
