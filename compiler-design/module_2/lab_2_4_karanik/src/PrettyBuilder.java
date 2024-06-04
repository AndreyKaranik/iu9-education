import java.util.List;

public class PrettyBuilder {
    private StringBuilder stringBuilder;
    private int indent;
    private int count;


    public PrettyBuilder(String name, int indent) {
        stringBuilder = new StringBuilder();
        stringBuilder.append(name).append("(");
        this.indent = indent + stringBuilder.length();
        count = 0;
    }
    public PrettyBuilder append(String name, List<? extends AbstractTree.PrettyPrintable> list) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=[");
        int size = indent + builder.length();
        if (!list.isEmpty()) {
            builder.append(list.get(0).toString(size));
            for (int i = 1; i < list.size(); i++) {
                builder.append(",\n").append(" ".repeat(Math.max(0, size))).append(list.get(i).toString(size));
            }
        }
        builder.append("]");
        stringBuilder.append(builder);
        count++;
        return this;
    }

    public PrettyBuilder append(String name, AbstractTree.PrettyPrintable prettyPrintable) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=");
        int size = indent + builder.length();
        builder.append(prettyPrintable.toString(size));
        stringBuilder.append(builder);
        count++;
        return this;
    }

    public PrettyBuilder append(String name, String value) {
        StringBuilder builder = new StringBuilder();
        if (count > 0) {
            builder.append(",\n").append(" ".repeat(Math.max(0, indent)));
        }
        builder.append(name).append("=");
        builder.append(value);
        stringBuilder.append(builder);
        count++;
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString() + ")";
    }
}
