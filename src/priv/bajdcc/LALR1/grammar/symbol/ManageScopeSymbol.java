package priv.bajdcc.LALR1.grammar.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import priv.bajdcc.LALR1.grammar.runtime.RuntimeObject;
import priv.bajdcc.LALR1.grammar.semantic.ISemanticRecorder;
import priv.bajdcc.LALR1.grammar.tree.Function;
import priv.bajdcc.LALR1.grammar.type.TokenTools;
import priv.bajdcc.util.HashListMap;
import priv.bajdcc.util.HashListMapEx;
import priv.bajdcc.util.Position;
import priv.bajdcc.util.lexer.token.Token;
import priv.bajdcc.util.lexer.token.TokenType;

/**
 * 命名空间管理
 *
 * @author bajdcc
 */
public class ManageScopeSymbol implements IQueryScopeSymbol, IQueryBlockSymbol,
		IManageDataSymbol, IManageScopeSymbol {

	private static String ENTRY_NAME = "main";
	private static String LAMBDA_PREFIX = "~lambda#";
	private int lambdaId = 0;
	private HashListMap<Object> symbolList = new HashListMap<Object>();
	private HashListMapEx<String, Function> funcMap = new HashListMapEx<String, Function>();
	private ArrayList<HashSet<String>> stkScope = new ArrayList<HashSet<String>>();
	private HashSet<String> symbolsInFutureBlock = new HashSet<String>();
	private HashMap<BlockType, Integer> blockLevel = new HashMap<BlockType, Integer>();
	private Stack<BlockType> blockStack = new Stack<BlockType>();

	public ManageScopeSymbol() {
		enterScope();
		funcMap.add(ENTRY_NAME, new Function());
		for (BlockType type : BlockType.values()) {
			blockLevel.put(type, 0);
		}
	}

	@Override
	public void enterScope() {
		stkScope.add(0, new HashSet<String>());
		for (String name : symbolsInFutureBlock) {
			registerSymbol(name);
		}
		clearFutureArgs();
	}

	@Override
	public void leaveScope() {
		stkScope.remove(0);
		clearFutureArgs();
	}

	@Override
	public void clearFutureArgs() {
		symbolsInFutureBlock.clear();
	}

	@Override
	public boolean findDeclaredSymbol(String name) {
		if (symbolsInFutureBlock.contains(name)) {
			return true;
		}
		for (HashSet<String> hashSet : stkScope) {
			if (hashSet.contains(name)) {
				return true;
			}
		}
		if (TokenTools.isExternalName(name)) {
			registerSymbol(name);
			return true;
		}
		return false;
	}

	@Override
	public boolean findDeclaredSymbolDirect(String name) {
		if (symbolsInFutureBlock.contains(name)) {
			return true;
		}
		return stkScope.get(0).contains(name);
	}

	@Override
	public boolean isUniqueSymbolOfBlock(String name) {
		return stkScope.get(0).contains(name);
	}

	@Override
	public String getEntryName() {
		return ENTRY_NAME;
	}

	@Override
	public Token getEntryToken() {
		Token token = new Token();
		token.kToken = TokenType.ID;
		token.object = getEntryName();
		token.position = new Position();
		return token;
	}

	@Override
	public Function getFuncByName(String name) {
		Function func = funcMap.get(name);
		if (func != null) {
			return func;
		}
		for (Function function : funcMap.list) {
			String funcName = function.getRealName();
			if (funcName != null && funcName.equals(name)) {
				return function;
			}
		}
		return null;
	}

	@Override
	public Function getFuncByRealName(String name) {
		for (Function func : funcMap.list) {
			if (name.equals(func.getRealName())) {
				return func;
			}
		}
		return null;
	}

	@Override
	public void registerSymbol(String name) {
		stkScope.get(0).add(name);
		symbolList.add(name);
	}

	@Override
	public void registeFunc(String name, Function func) {
		if (func.getName().kToken == TokenType.ID) {
			func.setRealName(func.getName().toRealString());
			symbolList.add(func.getRealName());
		} else {
			func.setRealName(LAMBDA_PREFIX + lambdaId++);
		}
		funcMap.add(func.getRealName(), func);
	}

	@Override
	public boolean isRegisteredFunc(String name) {
		Function func = funcMap.get(name);
		if (func == null) {
			return false;
		}
		return func.getRealName() != null;
	}

	@Override
	public boolean registerFutureSymbol(String name) {
		return symbolsInFutureBlock.add(name);
	}

	public void check(ISemanticRecorder recorder) {
		for (Function func : funcMap.list) {
			func.analysis(recorder);
		}
	}

	@Override
	public HashListMap<Object> getSymbolList() {
		return symbolList;
	}

	@Override
	public HashListMapEx<String, Function> getFuncMap() {
		return funcMap;
	}

	public String getSymbolString() {
		StringBuffer sb = new StringBuffer();
		sb.append("#### 符号表 ####");
		sb.append(System.getProperty("line.separator"));
		int i = 0;
		for (Object symbol : symbolList.list) {
			sb.append(i + ": " + "["
					+ RuntimeObject.fromObject(symbol).getName() + "] "
					+ symbol);
			sb.append(System.getProperty("line.separator"));
			i++;
		}
		return sb.toString();
	}

	public String getFuncString() {
		StringBuffer sb = new StringBuffer();
		sb.append("#### 过程表 ####");
		sb.append(System.getProperty("line.separator"));
		int i = 0;
		for (Function func : funcMap.list) {
			sb.append("----==== #" + i + " ====----");
			sb.append(System.getProperty("line.separator"));
			sb.append(func.toString());
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));
			i++;
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getSymbolString());
		sb.append(getFuncString());
		return sb.toString();
	}

	@Override
	public void enterBlock(BlockType type) {
		switch (type) {
		case kCycle:
			int level = blockLevel.get(type);
			blockLevel.put(type, level + 1);
			break;
		case kFunc:
		case kYield:
			blockStack.push(type);
			break;
		default:
			break;

		}
	}

	@Override
	public void leaveBlock(BlockType type) {
		switch (type) {
		case kCycle:
			int level = blockLevel.get(type);
			blockLevel.put(type, level - 1);
			break;
		case kFunc:
		case kYield:
			if (blockStack.peek() == type) {
				blockStack.pop();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean isInBlock(BlockType type) {
		switch (type) {
		case kCycle:
			return blockLevel.get(type) > 0;
		case kFunc:
		case kYield:
			return blockStack.isEmpty() ? false : (blockStack.peek() == type);
		default:
			break;
		}
		return false;
	}
}
