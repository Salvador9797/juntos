package doapp.juntos;
import java.util.*;
public class NoteChecklistChecks {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("Check "+checks);}
 public static void main(String[] args){
  String shopping="1 paq. De cebolla\n1 paq. De limón\n10 tomates\n1 ají morrón rojo\n2 paq. de lechuga repollada\n1 cartón de huevo\n2 leche evaporada marca bravo\n6 tunas en agua marca primavera\n6 plátanos maduros\n1 yautía coco\n1 mano de guineitos\n1 queso de hoja\n1 tortillas de tacos\n1 paquete de pechuga";
  List<NoteChecklist.Entry> list=NoteChecklist.parse(shopping);
  check(list.size()==14);check(list.get(0).text.equals("1 paq. De cebolla"));check(list.get(2).text.equals("10 tomates"));
  list=NoteChecklist.parse("1. Comprar leche\n2) Llamar a mamá");check(list.size()==2);check(list.get(0).text.equals("Comprar leche"));
  list=NoteChecklist.parse("- [x] Pan\n- [ ] Leche\n☑ Huevos\n☐ Queso");check(list.size()==4);check(list.get(0).done);check(!list.get(1).done);check(list.get(2).done);check(!list.get(3).done);
  check(NoteChecklist.parse("Una nota normal").isEmpty());check(NoteChecklist.parse("Nos vemos mañana.\nLleva el paraguas.").isEmpty());check(NoteChecklist.parse("Un párrafo\n\nOtro párrafo").isEmpty());
  check(NoteChecklist.parse("• Pan\r\n• Leche").size()==2);check(NoteChecklist.parse("1/2 libra de queso\n2.5 litros de agua").get(1).text.equals("2.5 litros de agua"));
  check(NoteChecklist.key("  PAN  integral ").equals(NoteChecklist.key("pan integral")));
  check(NoteChecklist.parse("- "+"a".repeat(501)+"\n- leche").isEmpty());
  check(NoteChecklist.parse("Comprar 2 productos para la cena de mañana y después pasar por casa de mi madre\nPan").isEmpty());
  System.out.println(checks+" comprobaciones de conversión correctas");
 }
}
