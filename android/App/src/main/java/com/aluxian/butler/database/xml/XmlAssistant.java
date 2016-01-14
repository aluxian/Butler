package com.aluxian.butler.database.xml;

import com.aluxian.butler.database.enums.FunctionType;
import com.aluxian.butler.database.enums.PatternType;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public final class XmlAssistant {

    @ElementList
    private List<Command> commands;

    public List<Command> getCommands() {
        return commands;
    }

    public static final class Command {

        @Element
        private PatternType type;

        @Element
        private String pattern;

        @Element
        private FunctionType function;

        @ElementList(required = false)
        private List<String> parameters;

        @ElementList(required = false)
        private List<Command> commands;

        public PatternType getType() {
            return type;
        }

        public String getPattern() {
            return pattern;
        }

        public FunctionType getFunction() {
            return function;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public List<Command> getCommands() {
            return commands;
        }

    }

}
