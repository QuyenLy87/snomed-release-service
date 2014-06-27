package org.ihtsdo.buildcloud.service.execution.database;

import java.util.ArrayList;
import java.util.List;

public class TableSchema {

	private String name;
	private List<Field> fields;

	public TableSchema(String name) {
		this.name = name;
		fields = new ArrayList<>();
	}

	public TableSchema field(String name, DataType type) {
		fields.add(new Field(name, type));
		return this;
	}

	public String getName() {
		return name;
	}

	public List<Field> getFields() {
		return fields;
	}

	static class Field {

		private String name;
		private DataType type;

		private Field(String name, DataType type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public DataType getType() {
			return type;
		}

	}

}