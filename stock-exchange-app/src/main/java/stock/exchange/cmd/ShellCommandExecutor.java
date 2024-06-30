package stock.exchange.cmd;

public interface ShellCommandExecutor {

  String BYE_STRING = "BYE";

  String execute(String line);

}
