package org.tue.thesis.dto;

import org.apache.commons.cli.CommandLine;

public class CommandLineParameters implements Parameters{
    private CommandLine cli;

    public CommandLineParameters(CommandLine cli) {
        this.cli = cli;
    }
    @Override
    public boolean isLocal() {
        return cli.hasOption("local");
    }

    @Override
    public String getInputPath() {
        return cli.getOptionValue("input");
    }

    @Override
    public String getOutputPath() {
        return cli.getOptionValue("output");
    }

    @Override
    public boolean hasQueryNumber() {
        return cli.hasOption("query");
    }

    @Override
    public int getQueryNumber() {
        return Integer.parseInt(cli.getOptionValue("query"));
    }

    @Override
    public ScalingFactor getScalingFactor() {
        var sf = cli.getOptionValue("sf");
        if ("1".equals(sf)) {
            return ScalingFactor.ONE;
        } else if ("10".equals(sf)) {
            return ScalingFactor.TEN;
        }
        throw new IllegalArgumentException("Invalid scaling factor");
    }

    @Override
    public int getNumRepetitions() {
        return cli.getOptionValue("repetitions") == null ? 1 :
                Integer.parseInt(cli.getOptionValue("repetitions"));
    }
}
