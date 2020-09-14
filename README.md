# Funz Plugin Cast3m

Cast3m plugin for Funz.

# calculator.xml for Funz

```
<CALCULATOR>
    <!-- localhost -->
    <HOST name="127.0.0.1"  port="19001" />
    <HOST name="127.0.0.1"  port="19002" />
    <HOST name="127.0.0.1"  port="19003" />
    <HOST name="127.0.0.1"  port="19004" />

    <CODE name='Cast3m' command='./scripts/Cast3m.sh' />

</CALCULATOR>
```

# calculator-localhost.xml for Promethee

```
<?xml version="1.0" encoding="UTF-8"?>
<CALCULATOR>
  <HOST name='localhost' port='19001'/>
  <HOST name='localhost' port='19002'/>
  <HOST name='localhost' port='19003'/>
  <HOST name='localhost' port='19004'/>
  <HOST name='localhost' port='19005'/>
  <HOST name='localhost' port='19006'/>
  <HOST name='localhost' port='19007'/>
  <HOST name='localhost' port='19008'/>
  <HOST name='localhost' port='19009'/>
  <HOST name='localhost' port='19010'/>
  
  <!-- script version -->
  <CODE name='Cast3m' command='./scripts/Cast3m.sh' />
  <!-- java version for castem2018 -->
  <CODE name='Cast3m' cplugin='file:./plugins/calc/Cast3m.cplugin.jar'/>
</CALCULATOR>
```