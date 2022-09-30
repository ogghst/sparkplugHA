package it.nm.sparkplugha.model;

import org.eclipse.tahu.message.model.MetricDataType;

public class SPHAMetric {

	private String name;
	private Object value;
	private MetricDataType type;

	protected SPHAMetric(String name, MetricDataType type, Object value) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public MetricDataType getType() {
		return type;
	}

	public void setType(MetricDataType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return name;
	}

}