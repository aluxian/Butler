package com.aluxian.butler.database.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.enums.FunctionType;
import com.aluxian.butler.database.enums.PatternType;

import java.util.List;

/**
 * Database model which stores commands that the assistant can do
 */
@Table(name = "ASSISTANT_COMMANDS")
public class AssistantCommand extends PatternModel {

    /** The function of the command */
    @Column(name = "Function")
    public FunctionType function;

    /** List of parameters for the {link #function} */
    @Column(name = "Parameters")
    public String[] parameters;

    /** Assistant commands with a parent will only be matched after their parent */
    @Column(name = "ParentCommand")
    public AssistantCommand parentCommand;

    @SuppressWarnings("UnusedDeclaration")
    public AssistantCommand() {
        super();
    }

    public AssistantCommand(PatternType type, String pattern, FunctionType function, List<String> parameters,
                            AssistantCommand parentCommand) {
        super();
        this.type = type;
        this.pattern = pattern;
        this.function = function;
        this.parameters = parameters != null ? parameters.toArray(new String[parameters.size()]) : null;
        this.parentCommand = parentCommand;
    }

    /**
     * @return The commands expectedInput from the user after this one
     */
    public List<AssistantCommand> getCommands() {
        return new Select().from(AssistantCommand.class).where("ParentCommand = ?", getId()).execute();
    }

    public static List<AssistantCommand> getDefaultQuestions() {
        return new Select().from(AssistantCommand.class).where("ParentCommand IS NULL").execute();
    }

}
