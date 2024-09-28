package com.mrboomdev.awery.ui.activity.setup;

import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.resolveDrawable;
import static com.mrboomdev.awery.app.AweryLifecycle.getContext;
import static com.mrboomdev.awery.util.NiceUtils.find;
import static com.mrboomdev.awery.util.io.FileUtil.readAssets;
import static com.mrboomdev.awery.util.ui.ViewUtil.clearImageTint;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setImageTintAttr;
import static com.mrboomdev.awery.util.ui.ViewUtil.setScale;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.databinding.ItemListSettingBinding;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.TabsTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupTabsAdapter extends RecyclerView.Adapter<SetupTabsAdapter.ViewHolder> {
	private final List<TabsTemplate> templates = new ArrayList<>();
	private Drawable selectedDrawable;
	private TabsTemplate selected;

	public SetupTabsAdapter() {
		try {
			var json = readAssets("tabs_templates.json");
			var adapter = Parser.<List<TabsTemplate>>getAdapter(List.class, TabsTemplate.class);

			// TODO: 6/27/2024 Return this option when custom feeds will be done
			/*templates.add(new TabsTemplate() {{
				this.id = "custom";
				this.title = "Custom";
				this.icon = "ic_settings_outlined";
				this.description = "Are you a power user who likes to customize everything? You must like it!";
			}});*/

			templates.addAll(Parser.fromString(adapter, json));

			var savedSelected = AwerySettings.TABS_TEMPLATE.getValue();
			selected = find(templates, template -> Objects.equals(template.id, savedSelected));
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
		var binding = ItemListSettingBinding.inflate(
				LayoutInflater.from(parent.getContext()), parent, false);

		setTopMargin(binding.title, dpPx(binding.title, -5));
		binding.checkbox.setVisibility(View.GONE);
		binding.toggle.setVisibility(View.GONE);
		binding.divider.setVisibility(View.GONE);
		binding.options.setVisibility(View.GONE);

		return new ViewHolder(binding);
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
		private final ItemListSettingBinding binding;
		private TabsTemplate template;

		public ViewHolder(@NonNull ItemListSettingBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(v -> {
				selected = template;
				notifyItemRangeChanged(0, templates.size());
			});

			/* We need only a single instance so we lazily init an shared object */
			if(selectedDrawable == null) {
				var background = new GradientDrawable();
				background.setCornerRadius(dpPx(binding, 16));
				background.setColor(resolveAttrColor(getContext(binding), com.google.android.material.R.attr.colorOnSecondary));
				selectedDrawable = background;
			}
		}

		public void bind(@NonNull TabsTemplate template) {
			this.template = template;

			binding.title.setText(template.title);
			binding.description.setText(template.description);
			binding.icon.setVisibility(template.icon != null ? View.VISIBLE : View.INVISIBLE);

			if(template.icon != null) {
				binding.icon.setImageDrawable(resolveDrawable(getContext(binding), template.icon));

				if(binding.icon.getDrawable() instanceof VectorDrawable) {
					setImageTintAttr(binding.icon, com.google.android.material.R.attr.colorOnSecondaryContainer);
					setScale(binding.icon, 1);
				} else {
					clearImageTint(binding.icon);
					setScale(binding.icon, 1.2f);
				}
			}

			binding.getRoot().setBackground(template != selected
					? null : selectedDrawable);
		}
	}
}