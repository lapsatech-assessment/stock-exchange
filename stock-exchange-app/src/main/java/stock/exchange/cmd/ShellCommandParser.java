package stock.exchange.cmd;

public interface ShellCommandParser {

  String BYE_STRING = "BYE";

  String execute(String line);

}
