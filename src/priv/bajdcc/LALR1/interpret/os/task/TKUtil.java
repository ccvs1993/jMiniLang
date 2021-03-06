package priv.bajdcc.LALR1.interpret.os.task;

import priv.bajdcc.LALR1.interpret.os.IOSCodePage;

/**
 * 【服务】工具
 *
 * @author bajdcc
 */
public class TKUtil implements IOSCodePage {
	@Override
	public String getName() {
		return "/task/util";
	}

	@Override
	public String getCode() {
		return "import \"sys.base\";\n" +
				"import \"sys.list\";\n" +
				"import \"sys.proc\";\n" +
				"import \"sys.task\";\n" +
				"import \"sys.string\";\n" +
				"import \"sys.func\";\n" +
				"\n" +
				"call g_set_process_desc(\"util service\");\n" +
				"call g_set_process_priority(73);\n" +
				"\n" +
				"var tid = 2;\n" +
				"var handle = call g_create_pipe(\"TASKSEND#\" + tid);\n" +
				"\n" +
				"call g_func_import_string_module();\n" +
				"\n" +
				"var time = func ~(msg, caller) {\n" +
				"    var id = call g_map_get(msg, \"id\");\n" +
				"    if (call g_is_null(id)) {\n" +
				"        call g_map_put(msg, \"error\", 1);\n" +
				"        call g_map_put(msg, \"val\", \"invalid task argument - id\");\n" +
				"        return;\n" +
				"    }\n" +
				"    var arg = call g_map_get(msg, \"arg\");\n" +
				"    var arr = call g_func_drop(arg, 2);\n" +
				"    if (call g_array_empty(arr)) { return; }\n" +
				"    if (id == \"calc\") {\n" +
				"        var str = call g_string_join_array(arr, \"\");\n" +
				"        var val = call g_task_calc(str);\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"sum\") {\n" +
				"        let arr = call g_func_fold(\"g_array_add\", arr, g_new_array, \"g_func_xsl\", \"g_string_atoi_s\", \"g_func_1\", \"g_not_null\");" +
				"        var val = call g_func_apply(\"g_func_add\", arr);\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"product\") {\n" +
				"        let arr = call g_func_fold(\"g_array_add\", arr, g_new_array, \"g_func_xsl\", \"g_string_atoi_s\", \"g_func_1\", \"g_not_null\");\n" +
				"        var val = call g_func_apply(\"g_func_mul\", arr);\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"reverse\") {\n" +
				"        var str = call g_string_join_array(arr, \" \");\n" +
				"        var val = call g_string_reverse(str);\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"palindrome\") {\n" +
				"        var str = call g_string_join_array(arr, \" \");\n" +
				"        var val = call g_func_applicative(\"g_func_eq\", str, \"g_string_reverse\");\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"toupper\") {\n" +
				"        let arr = call g_func_map(arr, \"g_string_toupper\");\n" +
				"        var val = call g_string_join_array(arr, \" \");\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"tolower\") {\n" +
				"        let arr = call g_func_map(arr, \"g_string_tolower\");\n" +
				"        var val = call g_string_join_array(arr, \" \");\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    } else if (id == \"doc\") {\n" +
				"        var val = call g_string_join_array(arr, \" \");\n" +
				"        let val = call g_doc(val);\n" +
				"        call g_map_put(msg, \"val\", val);\n" +
				"    }\n" +
				"};\n" +
				"\n" +
				"var handler = func ~(ch) {\n" +
				"    if (ch == 'E') {\n" +
				"        call g_destroy_pipe(handle);\n" +
				"        return;\n" +
				"    }\n" +
				"    var msg = call g_query_share(\"TASKDATA#\" + tid);\n" +
				"    var caller = call g_query_share(\"TASKCALLER#\" + tid);\n" +
				"    call time(msg, caller);\n" +
				"    var handle = call g_create_pipe(\"TASKRECV#\" + tid);\n" +
				"    var f = func ~(ch) {\n" +
				"        if (ch == 'E') { call g_destroy_pipe(handle); }" +
				"    };\n" +
				"    call g_read_pipe(handle, f);\n" +
				"};\n" +
				"\n" +
				"var data = {};\n" +
				"call g_task_add_proc(2, data);\n" +
				"\n" +
				"call g_read_pipe(handle, handler);\n";
	}
}
