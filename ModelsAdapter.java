package my.hf.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ModelsAdapter extends BaseAdapter {
    private Context context;
    private List<HuggingFaceModel> models;
    private LayoutInflater inflater;

    public ModelsAdapter(Context context, List<HuggingFaceModel> models) {
        this.context = context;
        this.models = models;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public Object getItem(int position) {
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.model_list_item, parent, false);
            holder = new ViewHolder();
            holder.modelName = (TextView) convertView.findViewById(R.id.modelName);
            holder.modelInfo = (TextView) convertView.findViewById(R.id.modelInfo);
            holder.modelTags = (TextView) convertView.findViewById(R.id.modelTags);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HuggingFaceModel model = models.get(position);
        holder.modelName.setText(model.getId());
        holder.modelInfo.setText(model.getDownloads() + " downloads");
        holder.modelTags.setText(model.getTags());

        return convertView;
    }

    private static class ViewHolder {
        TextView modelName;
        TextView modelInfo;
        TextView modelTags;
    }
}