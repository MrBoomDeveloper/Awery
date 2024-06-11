package com.mrboomdev.awery.ui.activity.setup;

import static com.mrboomdev.awery.util.NiceUtils.findIn;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.databinding.GridSimpleCardBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetupTabsAdapters extends RecyclerView.Adapter<SetupTabsAdapters.ViewHolder> {
	private final List<TabsTemplate> templates = new ArrayList<>();
	private TabsTemplate selected;

	public SetupTabsAdapters() {
		try {
			var json = readAssets("tabs_templates.json");
			var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);

			templates.add(null);
			templates.addAll(Parser.fromString(adapter, json));

			var savedSelected = AwerySettings.TABS_TEMPLATE.getValue();
			selected = findIn(template -> template != null && template.id.equals(savedSelected), templates);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public TabsTemplate getSelected() {
		return selected;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(GridSimpleCardBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bind(templates.get(position));
	}

	@Override
	public int getItemCount() {
		return templates.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final GridSimpleCardBinding binding;
		private TabsTemplate template;

		public ViewHolder(@NonNull GridSimpleCardBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(v -> {
				selected = template;
				notifyItemRangeChanged(0, templates.size());
			});
		}

		public void bind(@Nullable TabsTemplate template) {
			this.template = template;
			binding.title.setText(template != null ? template.title : "None");

			if(template == selected) {
				binding.getRoot().setBackgroundColor(0x55ff0000);
			} else {
				binding.getRoot().setBackgroundColor(0);
			}
		}
	}
}