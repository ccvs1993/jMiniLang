package priv.bajdcc.LALR1.interpret.module;

import org.apache.log4j.Logger;
import priv.bajdcc.LALR1.grammar.Grammar;
import priv.bajdcc.LALR1.grammar.runtime.*;
import priv.bajdcc.OP.grammar.error.GrammarException;
import priv.bajdcc.OP.grammar.handler.IPatternHandler;
import priv.bajdcc.OP.syntax.handler.SyntaxException;
import priv.bajdcc.util.lexer.error.RegexException;
import priv.bajdcc.util.lexer.token.OperatorType;
import priv.bajdcc.util.lexer.token.Token;
import priv.bajdcc.util.lexer.token.TokenType;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 【模块】服务模块
 *
 * @author bajdcc
 */
public class ModuleTask implements IInterpreterModule {

	private static ModuleTask instance = new ModuleTask();
	private static Logger logger = Logger.getLogger("task");

	public static ModuleTask getInstance() {
		return instance;
	}

	public static final int TASK_NUM = 16;

	@Override
	public String getModuleName() {
		return "sys.task";
	}

	@Override
	public RuntimeCodePage getCodePage() throws Exception {
		String base = "import \"sys.base\";\n" +
				"import \"sys.list\";\n" +
				"import \"sys.proc\";\n" +
				"\n" +
				"var g_task_init = func ~() {\n" +
				"    var task_table = [];\n" +
				"    call g_start_share(\"TASK#TABLE\", task_table);\n" +
				"    foreach (var i : call g_range(0, " + TASK_NUM + " - 1)) {\n" +
				"        call g_array_add(task_table, g_null);\n" +
				"    }\n" +
				"    var waiting_list = [];\n" +
				"    call g_start_share(\"TASK#LIST\", waiting_list);\n" +
				"};\n" +
				"export \"g_task_init\";\n" +
				"\n" +
				"var g_task_add_proc = func ~(no, data) {\n" +
				"    var task_table = call g_query_share(\"TASK#TABLE\");\n" +
				"    call g_array_set(task_table, no, data);\n" +
				"    call g_printdn(\"Task #\" + no + \" created\");\n" +
				"};\n" +
				"export \"g_task_add_proc\";\n" +
				"\n" +
				"var g_task_get = func ~(tid, msg) {\n" +
				"    var waiting_list = call g_query_share(\"TASK#LIST\");\n" +
				"    var pid = call g_get_pid();\n" +
				"    var m = {};\n" +
				"    call g_map_put(m, \"pid\", pid);\n" +
				"    call g_map_put(m, \"tid\", tid);\n" +
				"    call g_map_put(m, \"msg\", msg);\n" +
				"    call g_start_share(\"MSG#\" + pid, m);\n" +
				"    call g_lock_share(\"TASK#LIST\");\n" +
				"    call g_array_add(waiting_list, pid);\n" +
				"    call g_unlock_share(\"TASK#LIST\");\n" +
				"    var handle = call g_create_pipe(\"int#1\");\n" +
				"    call g_write_pipe(handle, '@');\n" +
				"    var h = call g_wait_pipe(\"IPC#\" + pid);\n" +
				"    var f = func ~(ch) {" +
				"        if (ch == 'E') { call g_destroy_pipe(h); }\n" +
				"    };\n" +
				"    call g_read_pipe(h, f);\n" +
				"    call g_stop_share(\"MSG#\" + pid);\n" +
				"};\n" +
				"export \"g_task_get\";\n" +
				"\n" +
				"var g_task_get_fast = func ~(tid, id) {\n" +
				"    var arg = [];\n" +
				"    call g_array_add(arg, tid);\n" +
				"    call g_array_add(arg, id);\n" +
				"    \n" +
				"    var msg = {};\n" +
				"    call g_map_put(msg, \"id\", id);\n" +
				"    call g_map_put(msg, \"arg\", arg);\n" +
				"    call g_task_get(tid, msg);\n" +
				"    var error = call g_map_get(msg, \"error\");\n" +
				"    var val = call g_map_get(msg, \"val\");\n" +
				"    if (error == 1) {\n" +
				"        return g_null;\n" +
				"    } else {\n" +
				"        return val;\n" +
				"    }\n" +
				"};\n" +
				"export \"g_task_get_fast\";\n" +
				"var g_task_get_fast_arg = func ~(tid, id, a) {\n" +
				"    var arg = [];\n" +
				"    call g_array_add(arg, tid);\n" +
				"    call g_array_add(arg, id);\n" +
				"    call g_array_add(arg, a);\n" +
				"    \n" +
				"    var msg = {};\n" +
				"    call g_map_put(msg, \"id\", id);\n" +
				"    call g_map_put(msg, \"arg\", arg);\n" +
				"    call g_task_get(tid, msg);\n" +
				"    var error = call g_map_get(msg, \"error\");\n" +
				"    var val = call g_map_get(msg, \"val\");\n" +
				"    if (error == 1) {\n" +
				"        return g_null;\n" +
				"    } else {\n" +
				"        return val;\n" +
				"    }\n" +
				"};\n" +
				"export \"g_task_get_fast_arg\";\n" +
				"\n" +
				"var task_handler = func ~(ch) {\n" +
				"    var waiting_list = call g_query_share(\"TASK#LIST\");\n" +
				"    var task_table = call g_query_share(\"TASK#TABLE\");\n" +
				"    call g_lock_share(\"TASK#LIST\");\n" +
				"    var pid = call g_array_get(waiting_list, 0);\n" +
				"    call g_array_remove(waiting_list, 0);\n" +
				"    call g_unlock_share(\"TASK#LIST\");\n" +
				"    var m = call g_query_share(\"MSG#\" + pid);\n" +
				"    var tid = call g_map_get(m, \"tid\");\n" +
				"    let tid = call g_task_get_id_by_name(tid);\n" +
				"    var msg = call g_map_get(m, \"msg\");\n" +
				"    if (call g_is_null(tid)) {\n" +
				"        call g_map_put(msg, \"error\", 1);\n" +
				"        call g_map_put(msg, \"val\", \"invalid task name\");\n" +
				"    } else {\n" +
				"        call g_map_put(msg, \"error\", 0);\n" +
				"        call g_start_share(\"TASKDATA#\" + tid, msg);\n" +
				"        call g_start_share(\"TASKCALLER#\" + tid, pid);\n" +
				"        var h = call g_create_pipe(\"TASKSEND#\" + tid);\n" +
				"        call g_write_pipe(h, ch);\n" +
				"        var h2 = call g_wait_pipe(\"TASKRECV#\" + tid);\n" +
				"        call g_write_pipe(h2, 'E');\n" +
				"        call g_stop_share(\"TASKDATA#\" + tid);\n" +
				"        call g_stop_share(\"TASKCALLER#\" + tid);\n" +
				"    }\n" +
				"    var handle = call g_create_pipe(\"IPC#\" + pid);\n" +
				"    call g_write_pipe(handle, 'E');\n" +
				"};\n" +
				"\n" +
				"var g_task_handler = func ~(ch) -> call task_handler(ch);\n" +
				"export \"g_task_handler\";\n" +
				"" +
				"var g_task_get_id_by_name = func ~(name) {\n" +
				"    var task_name_table = call g_query_share(\"TASK#NAMELIST\");\n" +
				"    foreach (var i : call g_range(0, " + TASK_NUM + " - 1)) {\n" +
				"        var t = call g_array_get(task_name_table, i);\n" +
				"        if (!call g_is_null(t) && t == name) {\n" +
				"            return i;\n" +
				"        }\n" +
				"    }\n" +
				"    return g_null;\n" +
				"};\n" +
				"export \"g_task_get_id_by_name\";\n" +
				"var g_task_sleep = func ~(second) {\n" +
				"    if (second < 1) { return; }\n" +
				"    var begin = call g_task_get_timestamp();\n" +
				"    var end = begin + second * 1000;\n" +
				"    while (begin < end) {\n" +
				"        let begin = call g_task_get_timestamp();\n" +
				"        call g_sleep(50);\n" +
				"    }\n" +
				"};\n" +
				"export \"g_task_sleep\";\n";

		Grammar grammar = new Grammar(base);
		RuntimeCodePage page = grammar.getCodePage();
		IRuntimeDebugInfo info = page.getInfo();
		buildSystemMethod(info);
		buildUtilMethod(info);

		return page;
	}

	private void buildSystemMethod(IRuntimeDebugInfo info) {
		info.addExternalFunc("g_task_get_time", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "获取当前时间";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return new RuntimeObjectType[] { RuntimeObjectType.kString };
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				String format = String.valueOf(args.get(0).getObj());
				return new RuntimeObject(new SimpleDateFormat(format).format(new Date()));
			}
		});
		info.addExternalFunc("g_task_get_timestamp", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "获取当前时间戳";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return null;
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				return new RuntimeObject(BigInteger.valueOf(System.currentTimeMillis()));
			}
		});
		info.addExternalFunc("g_task_get_timestamp", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "获取当前时间戳";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return null;
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				return new RuntimeObject(BigInteger.valueOf(System.currentTimeMillis()));
			}
		});
		info.addExternalFunc("g_task_get_pipe_stat", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "获取管道信息";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return null;
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				return new RuntimeObject(status.getService().getPipeService().stat());
			}
		});
		info.addExternalFunc("g_task_get_share_stat", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "获取共享信息";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return null;
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				return new RuntimeObject(status.getService().getShareService().stat());
			}
		});
	}

	private void buildUtilMethod(IRuntimeDebugInfo info) {
		info.addExternalFunc("g_task_calc", new IRuntimeDebugExec() {
			@Override
			public String getDoc() {
				return "四则运算";
			}

			@Override
			public RuntimeObjectType[] getArgsType() {
				return new RuntimeObjectType[]{RuntimeObjectType.kString};
			}

			@Override
			public RuntimeObject ExternalProcCall(List<RuntimeObject> args,
			                                      IRuntimeStatus status) throws Exception {
				String expr = String.valueOf(args.get(0).getObj());
				return new RuntimeObject(util_calc(expr));
			}
		});
	}

	private static String util_calc(String expr) {
		try {
			priv.bajdcc.OP.grammar.Grammar grammar = new priv.bajdcc.OP.grammar.Grammar(expr);
			grammar.addTerminal("i", TokenType.INTEGER, null);
			grammar.addTerminal("PLUS", TokenType.OPERATOR, OperatorType.PLUS);
			grammar.addTerminal("MINUS", TokenType.OPERATOR, OperatorType.MINUS);
			grammar.addTerminal("TIMES", TokenType.OPERATOR, OperatorType.TIMES);
			grammar.addTerminal("DIVIDE", TokenType.OPERATOR,
					OperatorType.DIVIDE);
			grammar.addTerminal("LPA", TokenType.OPERATOR, OperatorType.LPARAN);
			grammar.addTerminal("RPA", TokenType.OPERATOR, OperatorType.RPARAN);
			String[] nons = new String[]{"E", "T", "F"};
			for (String non : nons) {
				grammar.addNonTerminal(non);
			}
			grammar.addPatternHandler("1", new IPatternHandler() {
				@Override
				public Object handle(List<Token> tokens, List<Object> symbols) {
					return Integer.parseInt(tokens.get(0).object.toString());
				}

				@Override
				public String getPatternName() {
					return "操作数转换";
				}
			});
			grammar.addPatternHandler("010", new IPatternHandler() {
				@Override
				public Object handle(List<Token> tokens, List<Object> symbols) {
					int lop = (int) symbols.get(0);
					int rop = (int) symbols.get(1);
					Token op = tokens.get(0);
					if (op.kToken == TokenType.OPERATOR) {
						OperatorType kop = (OperatorType) op.object;
						switch (kop) {
							case PLUS:
								return lop + rop;
							case MINUS:
								return lop - rop;
							case TIMES:
								return lop * rop;
							case DIVIDE:
								if (rop == 0) {
									return lop;
								} else {
									return lop / rop;
								}
							default:
								return 0;
						}
					} else {
						return 0;
					}
				}

				@Override
				public String getPatternName() {
					return "二元运算";
				}
			});
			grammar.addPatternHandler("101", new IPatternHandler() {
				@Override
				public Object handle(List<Token> tokens, List<Object> symbols) {
					Token ltok = tokens.get(0);
					Token rtok = tokens.get(1);
					Object exp = symbols.get(0);
					if (ltok.object == OperatorType.LPARAN
							&& rtok.object == OperatorType.RPARAN) {// 判断括号
						return exp;
					}
					return null;
				}

				@Override
				public String getPatternName() {
					return "括号运算";
				}
			});
			grammar.infer("E -> E @PLUS T | E @MINUS T | T");
			grammar.infer("T -> T @TIMES F | T @DIVIDE F | F");
			grammar.infer("F -> @LPA E @RPA | @i");
			grammar.initialize("E");
			return String.valueOf(grammar.run());
		} catch (RegexException e) {
			logger.error("#CALC# Error: " + e.getPosition() + "," + e.getMessage());
		} catch (SyntaxException e) {
			logger.error("#CALC#Error: " + e.getPosition() + "," + e.getMessage() + " " + e.getInfo());
		} catch (GrammarException e) {
			logger.error("#CALC#Error: " + e.getPosition() + "," + e.getMessage() + " " + e.getInfo());
		}
		return "#CALC#Error";
	}
}
