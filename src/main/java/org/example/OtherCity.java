package org.example;

public class OtherCity {

    public OtherCity() {
    }

    public OtherCity(String state, String name) {
        this.state = state;
        this.name = name;
    }

    private Integer id;

    private String state;

    private String name;

    private Integer topId;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTopId() {
        return topId;
    }

    public void setTopId(Integer topId) {
        this.topId = topId;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        City other = (City) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getState() == null ? other.getState() == null : this.getState().equals(other.getState()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getTopId() == null ? other.getTopId() == null : this.getTopId().equals(other.getTopId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getState() == null) ? 0 : getState().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getTopId() == null) ? 0 : getTopId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", state=").append(state);
        sb.append(", name=").append(name);
        sb.append(", topId=").append(topId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
