package py.com.toyotoshi.jasper;

import java.util.regex.Pattern;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

/**
 *
 * @author fsanabria
 * @version 0.1.1
 *
 */
public class Importes extends JRDefaultScriptlet{

    private static final String[] UNIDADES = {"", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", 
        "siete ", "ocho ", "nueve "};
    private static final String[] DECENAS = {"diez ", "once ", "doce ", "trece ", "catorce ", 
        "quince ", "dieciseis ", "diecisiete ", "dieciocho ", "diecinueve", 
        "veinte ", "veintiun ", "veintidos ", "veintitres ", "veinticuatro ", 
        "veinticinco ", "veintiseis ", "veintisiete ", "veintiocho ", "veintinueve ", 
        "treinta ", "cuarenta ", "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa "};
    private static final String[] CENTENAS = {"", "ciento ", "doscientos ", "trecientos ", "cuatrocientos ", 
        "quinientos ", "seiscientos ", "setecientos ", "ochocientos ", "novecientos "};

   
    /**
     * Retorna un string que representa la descripción en letras del valor recibido como parámetro.<p>
     *
     * Ejemplo:
     * <p>
     * numeroATexto("123.00", true) retorna "Ciento veinte y tres con 00/100"
     * <p>
     * numeroATexto("123.00", false) retorna "Ciento veinte y tres"
     * <p>
     *
     * @param numero string que representa un importe
     * @param centimos boolean que especifica si se incluyen los céntimos
     *
     * @return Un string con la representación en letras del importe.
     */
    public String numeroATexto(String numero, boolean centimos) throws JRScriptletException {
        String literal = "";
        String parte_decimal;
        //si el numero utiliza (.) en lugar de (,) -> se reemplaza
        numero = numero.replace(".", ",");
        //si el numero no tiene parte decimal, se le agrega ,00
        if (!numero.contains(",")) {
            numero = numero + ",00";
        }
        //se valida formato de entrada -> 0,00 y 999 999 999,00
        if (Pattern.matches("\\d{1,15},\\d{1,2}", numero)) {
            //se divide el numero 0000000,00 -> entero y decimal
            String Num[] = numero.split(",");
            //de da formato al numero decimal
            parte_decimal = "con " + Num[1] + "/100";
            //se convierte el numero a literal
            if (Double.parseDouble(Num[0]) == 0) {//si el valor es cero
                literal = "cero ";
            } else if (Double.parseDouble(Num[0]) > 999999999999d) {
                literal = getBillones(Num[0]);
            } else if (Double.parseDouble(Num[0]) > 999999) {//si es millon
                literal = getMillones(Num[0]);
            } else if (Double.parseDouble(Num[0]) > 999) {//si es miles
                literal = getMiles(Num[0]);
            } else if (Double.parseDouble(Num[0]) > 99) {//si es centena
                literal = getCentenas(Num[0]);
            } else if (Double.parseDouble(Num[0]) > 9) {//si es decena
                literal = getDecenas(Num[0]);
            } else {//sino unidades -> 9
                literal = getUnidades(Num[0]);
            }

            literal = literal.substring(0, 1).toUpperCase() + literal.substring(1);
            //devuelve el resultado con centimos o sin centimos
            if (centimos) {
                return (literal + parte_decimal);
            } else {
                return (literal);
            }
        } else {//error, no es un numero o supera la cantidad de 12 digitos 999 999 999 999
            return literal = numero;
        }
    }

    private String getUnidades(String numero) {// 1 - 9
        //si tuviera algun 0 antes se lo quita -> 09 = 9 o 009=9
        String num = numero.substring(numero.length() - 1);
        return UNIDADES[Integer.parseInt(num)];
    }

    private String getDecenas(String num) {// 99
        int n = Integer.parseInt(num);
        if (n < 10) {//para casos como -> 01 - 09
            return getUnidades(num);
        } else if (n > 29) {//para 30...99
            String u = getUnidades(num);
            if (u.equals("")) { //para 20,30,40,50,60,70,80,90
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 17];
            } else {
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 17] + "y " + u;
            }
        } else if (n > 19) { // para 20..29
            return DECENAS[Integer.parseInt(num.substring(0, 2)) - 10];
        } else {//numeros entre 11 y 19
            return DECENAS[n - 10];
        }
    }

    private String getCentenas(String num) {// 999 o 099
        if (Integer.parseInt(num) > 99) {//es centena
            if (Integer.parseInt(num) == 100) {//caso especial
                return " cien ";
            } else {
                return CENTENAS[Integer.parseInt(num.substring(0, 1))] + getDecenas(num.substring(1));
            }
        } else {//por Ej. 099 
            //se quita el 0 antes de numeroATexto a decenas
            return getDecenas(Integer.parseInt(num) + "");
        }
    }

    private String getMiles(String numero) {// 999 999
        //obtiene las centenas
        String c = numero.substring(numero.length() - 3);
        //obtiene los miles
        String m = numero.substring(0, numero.length() - 3);
        String n = "";
        //se comprueba que miles tenga valor entero
        if (Integer.parseInt(m) > 0) {
            n = getCentenas(m);
            return n + "mil " + getCentenas(c);
        } else {
            return "" + getCentenas(c);
        }
    }

    private String getMillones(String numero) { //000 000 000        
        //se obtiene los miles
        String miles = numero.substring(numero.length() - 6);
        //se obtiene los millones
        String millones = numero.substring(0, numero.length() - 6);
        String n = "";
        if (millones.length() > 3) {
            n = getMiles(millones) + "millones ";
        } else if (millones.length() > 1) {
            n = getCentenas(millones) + "millones ";
        } else {
            n = getUnidades(millones) + (Integer.parseInt(millones) == 1 ? "millon " : "millones ");
        }
        return n + getMiles(miles);
    }
    
    private String getBillones(String numero) {
        String millones = numero.substring(numero.length() - 12);
        String billones = numero.substring(0, numero.length() - 12);
        String n = "";
        if (billones.length() > 1) {
            n = getCentenas(billones) + "billones ";
        } else {
            n = getUnidades(billones) + (Integer.parseInt(billones) == 1 ? "billon " : "billones ");
        }
        return n + getMillones(millones);
    }
}