package priv.bajdcc.LALR1.grammar.runtime.service;

import org.apache.log4j.Logger;
import priv.bajdcc.LALR1.grammar.runtime.RuntimeObject;
import priv.bajdcc.LALR1.grammar.runtime.data.RuntimeArray;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 【运行时】运行时管道服务
 *
 * @author bajdcc
 */
public class RuntimePipeService implements IRuntimePipeService {

	class PipeStruct {
		public String name;
		public Queue<Character> queue;

		public PipeStruct(String name) {
			this.name = name;
			this.queue = new ArrayDeque<>();
		}

		public String getName() {
			return name;
		}
	}

	private static Logger logger = Logger.getLogger("pipe");
	private static final int MAX_PIPE = 1000;
	private PipeStruct arrPipes[];
	private Set<Integer> setPipeId;
	private Map<String, Integer> mapPipeNames;
	private int cyclePtr = 0;

	public RuntimePipeService() {
		this.arrPipes = new PipeStruct[MAX_PIPE];
		this.setPipeId = new HashSet<>();
		this.mapPipeNames = new HashMap<>();
	}

	@Override
	public int create(String name) {
		if (setPipeId.size() >= MAX_PIPE) {
			return -1;
		}
		if (mapPipeNames.containsKey(name)) {
			return mapPipeNames.get(name);
		}
		int handle;
		for (;;) {
			if (arrPipes[cyclePtr] == null) {
				handle = cyclePtr;
				setPipeId.add(cyclePtr);
				mapPipeNames.put(name, cyclePtr);
				arrPipes[cyclePtr++] = new PipeStruct(name);
				if (cyclePtr >= MAX_PIPE) {
					cyclePtr -= MAX_PIPE;
				}
				break;
			}
			cyclePtr++;
			if (cyclePtr >= MAX_PIPE) {
				cyclePtr -= MAX_PIPE;
			}
		}
		logger.debug("Pipe #" + handle + " '" + name + "' created");
		return handle;
	}

	@Override
	public boolean destroy(int handle) {
		if (!setPipeId.contains(handle)) {
			return false;
		}
		logger.debug("Pipe #" + handle + " '" + arrPipes[handle].name + "' destroyed");
		mapPipeNames.remove(arrPipes[handle].name);
		arrPipes[handle] = null;
		setPipeId.remove(handle);
		return true;
	}

	@Override
	public boolean destroyByName(String name) {
		if (!mapPipeNames.containsKey(name)) {
			return false;
		}
		return destroy(mapPipeNames.get(name));
	}

	@Override
	public char read(int handle) {
		if (!setPipeId.contains(handle)) {
			return '\uffff';
		}
		PipeStruct ps = arrPipes[handle];
		if (ps.queue.isEmpty()) {
			return '\ufffe';
		}
		return ps.queue.poll();
	}

	@Override
	public boolean write(int handle, char ch) {
		return setPipeId.contains(handle) && arrPipes[handle].queue.add(ch);
	}

	@Override
	public boolean isEmpty(int handle) {
		return !setPipeId.contains(handle) || arrPipes[handle].queue.isEmpty();
	}

	@Override
	public boolean query(String name) {
		return mapPipeNames.containsKey(name);
	}

	@Override
	public long size() {
		return setPipeId.size();
	}

	@Override
	public RuntimeArray stat() {
		RuntimeArray array = new RuntimeArray();
		array.add(new RuntimeObject(String.format("   %-5s   %-15s   %-20s",
				"Id", "Name", "Queue")));
		mapPipeNames.values().stream().sorted(Comparator.naturalOrder())
				.collect(Collectors.toList())
				.forEach((value) -> {
					array.add(new RuntimeObject(String.format("   %-5s   %-15s   %-20s",
							String.valueOf(value), arrPipes[value].name, arrPipes[value].queue.size())));
				});
		return array;
	}
}
