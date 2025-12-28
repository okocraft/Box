package net.okocraft.box.feature.stats.model;

public record StockStatistics(int amount, int rank, float percentage) {

    public static final StockStatistics EMPTY = new StockStatistics(0, 0, 0.0f);

}
