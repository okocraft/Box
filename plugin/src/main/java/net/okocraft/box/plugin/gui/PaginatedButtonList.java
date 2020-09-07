package net.okocraft.box.plugin.gui;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.jetbrains.annotations.Nullable;

import net.okocraft.box.plugin.gui.button.Button;

public class PaginatedButtonList {

    /** Table<row: page, column: slot, button> */
    private final Table<Integer, Integer, Button> buttons = HashBasedTable.create();

    /**
     * そのpageに含まれるボタンと、配置されているslotのマップを取得する。返されるマップを編集すると、そのページのボタンが変更される。
     * 
     * @param page ページ
     * @return スロットとボタンのマップ
     */
    public Map<Integer, Button> getPageButtons(int page) {
        return buttons.row(page);
    }

    /**
     * あるページのあるスロットのボタンを取得する。
     * 
     * @param page ページ
     * @param slot スロット
     * @return ボタン。設定されていない場合はnullを返す。
     */
    public @Nullable Button getButton(int page, int slot) {
        return buttons.row(page).get(slot);
    }

    /**
     * あるページのあるスロットのボタンを設定する。
     * 
     * @param page ページ
     * @param slot スロット
     * @param button ボタン。未設定にするときはnullを指定する。
     * @return もともとその位置に設定されていたボタン。未設定のときはnullを返す。
     */
    public @Nullable Button putButton(int page, int slot, @Nullable Button button) {
        return buttons.put(page, slot, button);
    }

    /**
     * すべてのページのすべてのボタンを取得する。取得されたボタンを編集すると、格納されているボタンが編集される（取得できるボタンはコピーではない）。
     * 
     * @return すべてのボタン
     */
    public Collection<Button> getAllButtons() {
        return buttons.values();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PaginatedButtonList)) {
            return false;
        }
        PaginatedButtonList paginatedButtonList = (PaginatedButtonList) o;
        return Objects.equals(buttons, paginatedButtonList.buttons);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(buttons);
    }
}
